package eventos.piura.services.impl;

import eventos.piura.dto.checkout.MetodoPagoGuardadoView;
import eventos.piura.model.MetodoPagoGuardado;
import eventos.piura.model.Usuario;
import eventos.piura.model.enums.MetodoPago;
import eventos.piura.repository.MetodoPagoGuardadoRepository;
import eventos.piura.services.MetodoPagoGuardadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Year;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MetodoPagoGuardadoServiceImpl implements MetodoPagoGuardadoService {

    private final MetodoPagoGuardadoRepository metodoPagoGuardadoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MetodoPagoGuardadoView> listarGuardados(UUID usuarioId) {
        return metodoPagoGuardadoRepository.findByUsuarioIdAndActivoTrueOrderByCreadoEnDesc(usuarioId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MetodoPagoGuardado> obtenerParaUsuario(UUID metodoId, UUID usuarioId) {
        return metodoPagoGuardadoRepository.findByIdAndUsuarioId(metodoId, usuarioId)
                .filter(MetodoPagoGuardado::isActivo);
    }

    @Override
    public MetodoPagoGuardado guardarTarjeta(Usuario usuario,
                                             String numero,
                                             String expiracion,
                                             String titular,
                                             String alias) {
        String limpio = limpiarDigitos(numero);
        if (limpio.length() < 12) {
            throw new IllegalArgumentException("Numero de tarjeta invalido.");
        }
        Expiracion exp = parseExpiracion(expiracion);
        String fingerprint = fingerprint("TARJETA:" + limpio + ":" + exp.mes() + ":" + exp.anio() + ":" + normalizar(titular));

        MetodoPagoGuardado guardado = metodoPagoGuardadoRepository
                .findByUsuarioIdAndFingerprint(usuario.getId(), fingerprint)
                .orElseGet(() -> {
                    MetodoPagoGuardado nuevo = new MetodoPagoGuardado();
                    nuevo.setUsuario(usuario);
                    nuevo.setTipo(MetodoPago.TARJETA);
                    nuevo.setFingerprint(fingerprint);
                    nuevo.setIdentificador(ultimosDigitos(limpio));
                    nuevo.setMascara(formatearMascara(limpio));
                    nuevo.setMarca(detectarMarca(limpio));
                    return nuevo;
                });

        guardado.setActivo(true);
        guardado.setTitular(normalizarTitulo(titular));
        guardado.setExpMes(exp.mes());
        guardado.setExpAnio(exp.anio());
        guardado.setAlias(normalizarAlias(alias, guardado));
        return metodoPagoGuardadoRepository.save(guardado);
    }

    @Override
    public MetodoPagoGuardado guardarWallet(Usuario usuario,
                                            MetodoPago tipo,
                                            String telefono,
                                            String alias) {
        if (tipo != MetodoPago.YAPE && tipo != MetodoPago.PLIN) {
            throw new IllegalArgumentException("Metodo no soportado para wallet: " + tipo);
        }

        String sanitizado = limpiarDigitos(telefono);
        if (!StringUtils.hasText(sanitizado)) {
            throw new IllegalArgumentException("Debes registrar un numero valido.");
        }

        String fingerprint = fingerprint(tipo.name() + ":" + sanitizado);

        MetodoPagoGuardado guardado = metodoPagoGuardadoRepository
                .findByUsuarioIdAndFingerprint(usuario.getId(), fingerprint)
                .orElseGet(() -> {
                    MetodoPagoGuardado nuevo = new MetodoPagoGuardado();
                    nuevo.setUsuario(usuario);
                    nuevo.setTipo(tipo);
                    nuevo.setFingerprint(fingerprint);
                    nuevo.setIdentificador(sanitizado);
                    nuevo.setTelefono(sanitizado);
                    nuevo.setMascara(formatearTelefono(sanitizado));
                    return nuevo;
                });

        guardado.setActivo(true);
        guardado.setAlias(StringUtils.hasText(alias) ? alias.trim() : aliasPorDefectoWallet(tipo, guardado));
        guardado.setMarca(null);
        guardado.setTitular(null);
        guardado.setExpMes(null);
        guardado.setExpAnio(null);
        return metodoPagoGuardadoRepository.save(guardado);
    }

    private MetodoPagoGuardadoView toView(MetodoPagoGuardado metodo) {
        String etiquetaBase = StringUtils.hasText(metodo.getAlias())
                ? metodo.getAlias()
                : etiquetaPorDefecto(metodo);
        String descripcion = descripcion(metodo);
        return new MetodoPagoGuardadoView(
                metodo.getId(),
                metodo.getTipo(),
                etiquetaBase,
                descripcion,
                detalle(metodo)
        );
    }

    private String descripcion(MetodoPagoGuardado metodo) {
        return switch (metodo.getTipo()) {
            case TARJETA -> {
                String exp = metodo.getExpMes() != null && metodo.getExpAnio() != null
                        ? String.format(Locale.ROOT, "%02d/%02d", metodo.getExpMes(), metodo.getExpAnio() % 100)
                        : "";
                yield (metodo.getMarca() != null ? metodo.getMarca() + " " : "") +
                        (metodo.getMascara() != null ? metodo.getMascara() : "") +
                        (StringUtils.hasText(exp) ? " · Exp " + exp : "");
            }
            case YAPE, PLIN -> metodo.getMascara() != null ? metodo.getMascara() : metodo.getIdentificador();
            case BILLETERA -> "Billetera";
        };
    }

    private String detalle(MetodoPagoGuardado metodo) {
        return switch (metodo.getTipo()) {
            case TARJETA -> metodo.getTitular();
            case YAPE, PLIN -> metodo.getTelefono();
            case BILLETERA -> null;
        };
    }

    private String etiquetaPorDefecto(MetodoPagoGuardado metodo) {
        return switch (metodo.getTipo()) {
            case TARJETA -> (metodo.getMarca() != null ? metodo.getMarca() : "Tarjeta") + " " + metodo.getIdentificador();
            case YAPE, PLIN -> metodo.getTipo().name() + " " + metodo.getIdentificador();
            case BILLETERA -> "Billetera digital";
        };
    }

    private String aliasPorDefectoWallet(MetodoPago tipo, MetodoPagoGuardado guardado) {
        return tipo.name() + " " + guardado.getIdentificador();
    }

    private Expiracion parseExpiracion(String expiracion) {
        if (!StringUtils.hasText(expiracion)) {
            throw new IllegalArgumentException("La expiracion de la tarjeta es requerida.");
        }
        String limpia = expiracion.trim();
        if (!limpia.matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
            throw new IllegalArgumentException("Formato de expiracion invalido (MM/AA).");
        }
        int mes = Integer.parseInt(limpia.substring(0, 2));
        int anio = Integer.parseInt(limpia.substring(3, 5));
        int anioCompleto = (Year.now().getValue() / 100) * 100 + anio;
        return new Expiracion(mes, anioCompleto);
    }

    private String fingerprint(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo generar la huella del metodo de pago.", e);
        }
    }

    private String limpiarDigitos(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replaceAll("\\D", "");
    }

    private String ultimosDigitos(String numero) {
        if (numero.length() <= 4) {
            return numero;
        }
        return numero.substring(numero.length() - 4);
    }

    private String formatearMascara(String numero) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numero.length(); i++) {
            if (i < numero.length() - 4) {
                sb.append('*');
            } else {
                sb.append(numero.charAt(i));
            }
            if ((i + 1) % 4 == 0 && i < numero.length() - 1) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    private String formatearTelefono(String numero) {
        if (numero.length() <= 3) {
            return numero;
        }
        return numero.substring(0, 3) + " **** " + numero.substring(numero.length() - 2);
    }

    private String detectarMarca(String numero) {
        if (numero.startsWith("4")) {
            return "Visa";
        }
        if (numero.startsWith("5")) {
            return "Mastercard";
        }
        if (numero.startsWith("3")) {
            return "Amex";
        }
        if (numero.startsWith("6")) {
            return "Discover";
        }
        return "Tarjeta";
    }

    private String normalizar(String valor) {
        if (!StringUtils.hasText(valor)) {
            return "";
        }
        return valor.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarTitulo(String valor) {
        if (!StringUtils.hasText(valor)) {
            return null;
        }
        return valor.trim();
    }

    private String normalizarAlias(String alias, MetodoPagoGuardado guardado) {
        if (!StringUtils.hasText(alias)) {
            return etiquetaPorDefecto(guardado);
        }
        return alias.trim();
    }

    private record Expiracion(int mes, int anio) {
    }
}
