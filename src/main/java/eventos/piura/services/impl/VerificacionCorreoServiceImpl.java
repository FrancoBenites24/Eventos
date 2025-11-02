package eventos.piura.services.impl;

import eventos.piura.model.Usuario;
import eventos.piura.model.VerificacionCorreo;
import eventos.piura.repository.UsuarioRepository;
import eventos.piura.repository.VerificacionCorreoRepository;
import eventos.piura.services.VerificacionCorreoService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificacionCorreoServiceImpl implements VerificacionCorreoService {

    private static final char[] CODIGO_ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int CODIGO_LONGITUD = 6;

    private final JavaMailSender mailSender;
    private final VerificacionCorreoRepository verificacionCorreoRepository;
    private final UsuarioRepository usuarioRepository;
    private final String verificationBaseUrl;
    private final String fromName;
    private final String fromAddress;
    private final long expirationHours;
    private final SecureRandom random = new SecureRandom();

    public VerificacionCorreoServiceImpl(JavaMailSender mailSender,
                                         VerificacionCorreoRepository verificacionCorreoRepository,
                                         UsuarioRepository usuarioRepository,
                                         @Value("${app.mail.verification-base-url:http://localhost:8080}") String verificationBaseUrl,
                                         @Value("${app.mail.from-name:Eventos}") String fromName,
                                         @Value("${spring.mail.username}") String fromAddress,
                                         @Value("${app.mail.verification-expiration-hours:24}") long expirationHours) {
        this.mailSender = mailSender;
        this.verificacionCorreoRepository = verificacionCorreoRepository;
        this.usuarioRepository = usuarioRepository;
        this.verificationBaseUrl = limpiarBaseUrl(verificationBaseUrl);
        this.fromName = fromName;
        this.fromAddress = fromAddress;
        this.expirationHours = expirationHours <= 0 ? 24 : expirationHours;
    }

    private String limpiarBaseUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "http://localhost:8080";
        }
        return raw.endsWith("/") ? raw.substring(0, raw.length() - 1) : raw;
    }

    @Override
    @Transactional
    public void crearSolicitudVerificacion(Usuario usuario) {
        if (usuario.isCorreoVerificado()) {
            return;
        }

        OffsetDateTime ahora = OffsetDateTime.now();
        verificacionCorreoRepository.deleteByUsuarioIdAndConsumidoEnIsNullAndVenceEnBefore(usuario.getId(), ahora);

        Optional<VerificacionCorreo> existente = verificacionCorreoRepository
                .findTopByUsuarioIdOrderByCreadoEnDesc(usuario.getId())
                .filter(v -> v.getConsumidoEn() == null)
                .filter(v -> v.getVenceEn().isAfter(ahora))
                .filter(v -> v.getCreadoEn() != null && Duration.between(v.getCreadoEn(), ahora).toMinutes() < 2);

        VerificacionCorreo verificacion = existente.orElseGet(() -> {
            VerificacionCorreo nuevo = new VerificacionCorreo();
            nuevo.setUsuario(usuario);
            nuevo.setCodigo(generarCodigo());
            nuevo.setVenceEn(ahora.plusHours(expirationHours));
            return verificacionCorreoRepository.save(nuevo);
        });

        enviarCorreo(usuario, verificacion);
    }

    @Override
    @Transactional
    public boolean confirmarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return false;
        }

        OffsetDateTime ahora = OffsetDateTime.now();

        Optional<VerificacionCorreo> posible = verificacionCorreoRepository.findByCodigo(codigo.trim());
        if (posible.isEmpty()) {
            return false;
        }

        VerificacionCorreo verificacion = posible.get();
        if (verificacion.getConsumidoEn() != null || verificacion.getVenceEn().isBefore(ahora)) {
            return false;
        }

        Usuario usuario = verificacion.getUsuario();
        usuario.setCorreoVerificado(true);
        usuarioRepository.save(usuario);

        verificacion.setConsumidoEn(ahora);
        verificacionCorreoRepository.save(verificacion);

        limpiarSolicitudesPendientes(usuario.getId(), verificacion.getId(), ahora);

        return true;
    }

    private void limpiarSolicitudesPendientes(UUID usuarioId, UUID verificacionActualId, OffsetDateTime ahora) {
        verificacionCorreoRepository.deleteByUsuarioIdAndConsumidoEnIsNullAndVenceEnBefore(usuarioId, ahora);
        verificacionCorreoRepository.findByUsuarioIdAndConsumidoEnIsNull(usuarioId).stream()
                .filter(v -> !Objects.equals(v.getId(), verificacionActualId))
                .forEach(v -> {
                    v.setConsumidoEn(ahora);
                    verificacionCorreoRepository.save(v);
                });
    }

    private void enviarCorreo(Usuario usuario, VerificacionCorreo verificacion) {
        String enlace = UriComponentsBuilder.fromUriString(verificationBaseUrl)
                .path("/auth/confirmar")
                .queryParam("codigo", verificacion.getCodigo())
                .build()
                .toUriString();

        String asunto = "Verifica tu correo electrónico";
        String nombreCompleto = (usuario.getNombre() + " " + usuario.getApellido()).trim();
        String cuerpo = """
                <p>Hola %s,</p>
                <p>Gracias por registrarte en Eventos Piura. Para activar tu cuenta es necesario verificar tu correo electrónico.</p>
                <p><strong>Código de verificación:</strong> %s</p>
                <p>Puedes completar la verificación haciendo clic en el siguiente enlace:</p>
                <p><a href="%s" style="background-color:#00a6c9;color:#fff;padding:10px 18px;border-radius:6px;text-decoration:none;">Verificar mi correo</a></p>
                <p>Si el botón no funciona, copia y pega esta URL en tu navegador:</p>
                <p><code>%s</code></p>
                <p>Este código vence en %d horas.</p>
                <p>Si no creaste esta cuenta, puedes ignorar este mensaje.</p>
                <p>Equipo de Eventos Piura</p>
                """.formatted(nombreCompleto, verificacion.getCodigo(), enlace, enlace, expirationHours);

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, StandardCharsets.UTF_8.name());
            helper.setTo(usuario.getCorreo());
            helper.setSubject(asunto);
            helper.setFrom(new InternetAddress(fromAddress, fromName));
            helper.setText(cuerpo, true);
            mailSender.send(mensaje);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("No se pudo enviar el correo de verificación", e);
        }
    }

    private String generarCodigo() {
        StringBuilder builder = new StringBuilder(CODIGO_LONGITUD);
        for (int i = 0; i < CODIGO_LONGITUD; i++) {
            builder.append(CODIGO_ALFABETO[random.nextInt(CODIGO_ALFABETO.length)]);
        }
        return builder.toString();
    }
}
