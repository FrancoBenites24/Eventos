package eventos.piura.services.impl;

import java.util.regex.Pattern;

import eventos.piura.model.RecuperacionContrasena;
import eventos.piura.model.Usuario;
import eventos.piura.repository.RecuperacionContrasenaRepository;
import eventos.piura.repository.UsuarioRepository;
import eventos.piura.services.RecuperacionContrasenaService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecuperacionContrasenaServiceImpl implements RecuperacionContrasenaService {

    private static final String TOKEN_ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TOKEN_LONGITUD = 48;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,72}$"
    );

    private final JavaMailSender mailSender;
    private final UsuarioRepository usuarioRepository;
    private final RecuperacionContrasenaRepository recuperacionContrasenaRepository;
    private final PasswordEncoder passwordEncoder;
    private final String baseUrl;
    private final String fromName;
    private final String fromAddress;
    private final long expirationMinutes;
    private final SecureRandom random = new SecureRandom();

    public RecuperacionContrasenaServiceImpl(JavaMailSender mailSender,
                                             UsuarioRepository usuarioRepository,
                                             RecuperacionContrasenaRepository recuperacionContrasenaRepository,
                                             PasswordEncoder passwordEncoder,
                                             @Value("${app.mail.reset-base-url:http://localhost:8080}") String baseUrl,
                                             @Value("${app.mail.from-name:Eventos}") String fromName,
                                             @Value("${spring.mail.username}") String fromAddress,
                                             @Value("${app.mail.reset-expiration-minutes:60}") long expirationMinutes) {
        this.mailSender = mailSender;
        this.usuarioRepository = usuarioRepository;
        this.recuperacionContrasenaRepository = recuperacionContrasenaRepository;
        this.passwordEncoder = passwordEncoder;
        this.baseUrl = normalizarBaseUrl(baseUrl);
        this.fromName = fromName;
        this.fromAddress = fromAddress;
        this.expirationMinutes = expirationMinutes <= 0 ? 60 : expirationMinutes;
    }

    private String normalizarBaseUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "http://localhost:8080";
        }
        return raw.endsWith("/") ? raw.substring(0, raw.length() - 1) : raw;
    }

    @Override
    @Transactional
    public void solicitarRecuperacion(String correo) {
        if (correo == null || correo.isBlank()) {
            return;
        }

        Optional<Usuario> posible = usuarioRepository.findByCorreoIgnoreCase(correo.trim());
        if (posible.isEmpty()) {
            return;
        }

        Usuario usuario = posible.get();
        if (!usuario.isCorreoVerificado()) {
            return;
        }

        OffsetDateTime ahora = OffsetDateTime.now();
        recuperacionContrasenaRepository.deleteByUsuarioIdAndConsumidoEnIsNullAndVenceEnBefore(usuario.getId(), ahora);

        Optional<RecuperacionContrasena> existente = recuperacionContrasenaRepository
                .findTopByUsuarioIdOrderByCreadoEnDesc(usuario.getId())
                .filter(t -> t.getConsumidoEn() == null)
                .filter(t -> t.getVenceEn().isAfter(ahora))
                .filter(t -> t.getCreadoEn() != null && Duration.between(t.getCreadoEn(), ahora).toMinutes() < 2);

        RecuperacionContrasena token = existente.orElseGet(() -> {
            RecuperacionContrasena nuevo = new RecuperacionContrasena();
            nuevo.setUsuario(usuario);
            nuevo.setToken(generarToken());
            nuevo.setVenceEn(ahora.plusMinutes(expirationMinutes));
            return recuperacionContrasenaRepository.save(nuevo);
        });

        enviarCorreo(usuario, token);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tokenValido(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        return recuperacionContrasenaRepository.findByToken(token.trim())
                .filter(t -> t.getConsumidoEn() == null)
                .filter(t -> t.getVenceEn().isAfter(ahora))
                .isPresent();
    }

    @Override
    @Transactional
    public boolean restablecerContrasena(String token, String nuevaContrasena) {
        if (token == null || token.isBlank() || nuevaContrasena == null) {
            return false;
        }
        String passwordLimpia = nuevaContrasena.trim();
        if (!contrasenaValida(passwordLimpia)) {
            return false;
        }

        OffsetDateTime ahora = OffsetDateTime.now();
        Optional<RecuperacionContrasena> posible = recuperacionContrasenaRepository.findByToken(token.trim());
        if (posible.isEmpty()) {
            return false;
        }

        RecuperacionContrasena recuperacion = posible.get();
        if (recuperacion.getConsumidoEn() != null || recuperacion.getVenceEn().isBefore(ahora)) {
            return false;
        }

        Usuario usuario = recuperacion.getUsuario();
        usuario.setContrasenaHash(passwordEncoder.encode(passwordLimpia));
        usuarioRepository.save(usuario);

        recuperacion.setConsumidoEn(ahora);
        recuperacionContrasenaRepository.save(recuperacion);

        limpiarPendientes(usuario.getId(), recuperacion.getId(), ahora);
        return true;
    }

    private boolean contrasenaValida(String valor) {
        return valor != null && PASSWORD_PATTERN.matcher(valor).matches();
    }

    private void limpiarPendientes(UUID usuarioId, UUID tokenActual, OffsetDateTime ahora) {
        recuperacionContrasenaRepository.deleteByUsuarioIdAndConsumidoEnIsNullAndVenceEnBefore(usuarioId, ahora);
        List<RecuperacionContrasena> activos = recuperacionContrasenaRepository.findByUsuarioIdAndConsumidoEnIsNull(usuarioId);
        for (RecuperacionContrasena pendiente : activos) {
            if (!pendiente.getId().equals(tokenActual)) {
                pendiente.setConsumidoEn(ahora);
                recuperacionContrasenaRepository.save(pendiente);
            }
        }
    }

    private String generarToken() {
        StringBuilder builder = new StringBuilder(TOKEN_LONGITUD);
        for (int i = 0; i < TOKEN_LONGITUD; i++) {
            builder.append(TOKEN_ALFABETO.charAt(random.nextInt(TOKEN_ALFABETO.length())));
        }
        return builder.toString();
    }

    private void enviarCorreo(Usuario usuario, RecuperacionContrasena token) {
        String enlace = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/auth/restablecer")
                .queryParam("token", token.getToken())
                .build()
                .toUriString();

        String asunto = "Instrucciones para restablecer tu contrasena";
        String nombre = (usuario.getNombre() + " " + usuario.getApellido()).trim();
        String cuerpo = """
                <p>Hola %s,</p>
                <p>Recibimos una solicitud para restablecer tu contrasena de Eventos Piura.</p>
                <p>Si fuiste tu, usa el siguiente enlace para definir una nueva contrasena:</p>
                <p><a href="%s" style="background-color:#00a6c9;color:#fff;padding:10px 18px;border-radius:6px;text-decoration:none;">Restablecer contrasena</a></p>
                <p>Tambien puedes copiar y pegar esta URL en tu navegador:</p>
                <p><code>%s</code></p>
                <p>El enlace caduca en %d minutos. Si no solicitaste este cambio, puedes ignorar el mensaje.</p>
                <p>Equipo de Eventos Piura</p>
                """.formatted(nombre, enlace, enlace, expirationMinutes);

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, StandardCharsets.UTF_8.name());
            helper.setTo(usuario.getCorreo());
            helper.setSubject(asunto);
            helper.setFrom(new InternetAddress(fromAddress, fromName));
            helper.setText(cuerpo, true);
            mailSender.send(mensaje);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("No se pudo enviar el correo de recuperacion", e);
        }
    }
}
