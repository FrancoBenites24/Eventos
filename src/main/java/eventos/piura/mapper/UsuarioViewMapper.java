package eventos.piura.mapper;

import eventos.piura.dto.UsuarioResumenView;
import eventos.piura.model.Usuario;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Component
public class UsuarioViewMapper {

    private static final Locale LOCALE_ES = new Locale("es");
    private static final DateTimeFormatter MIEMBRO_DESDE_FORMATTER =
            DateTimeFormatter.ofPattern("LLLL 'de' yyyy", LOCALE_ES);

    public UsuarioResumenView mapear(Usuario usuario) {
        if (usuario == null) {
            return new UsuarioResumenView(
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "ACTIVO",
                    false,
                    "",
                    "U"
            );
        }

        String nombre = valorSeguro(usuario.getNombre());
        String apellido = valorSeguro(usuario.getApellido());

        return new UsuarioResumenView(
                nombre,
                apellido,
                construirNombreCompleto(nombre, apellido),
                valorSeguro(usuario.getUsername()),
                valorSeguro(usuario.getCorreo()),
                valorSeguro(usuario.getTelefono()),
                usuario.getEstado() != null ? usuario.getEstado().name() : "ACTIVO",
                usuario.isCorreoVerificado(),
                formatearFecha(usuario.getCreadoEn()),
                calcularIniciales(nombre, apellido)
        );
    }

    private String valorSeguro(String valor) {
        return Optional.ofNullable(valor).orElse("");
    }

    private String construirNombreCompleto(String nombre, String apellido) {
        String full = (nombre + " " + apellido).trim();
        return full.isEmpty() ? "Usuario" : full;
    }

    private String formatearFecha(OffsetDateTime fecha) {
        if (fecha == null) {
            return "";
        }
        return MIEMBRO_DESDE_FORMATTER.format(fecha);
    }

    private String calcularIniciales(String nombre, String apellido) {
        StringBuilder sb = new StringBuilder();
        if (!nombre.isEmpty()) {
            sb.append(nombre.charAt(0));
        }
        if (!apellido.isEmpty()) {
            sb.append(apellido.charAt(0));
        }
        if (sb.length() == 0) {
            sb.append("U");
        }
        return sb.toString().toUpperCase(LOCALE_ES);
    }
}
