package eventos.piura.dto;

public record UsuarioResumenView(
        String nombre,
        String apellido,
        String nombreCompleto,
        String username,
        String correo,
        String telefono,
        String estado,
        boolean correoVerificado,
        String creadoEn,
        String iniciales
) {
}
