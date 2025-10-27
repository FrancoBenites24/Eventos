package eventos.piura.dto.organizador;

public record OrganizadorEventoCard(
        String estado,
        String categoria,
        String titulo,
        String fecha,
        String lugar,
        String entradas,
        String ingresos,
        String progreso,
        String detalleUrl,
        String editarUrl,
        String eliminarUrl
) {
}
