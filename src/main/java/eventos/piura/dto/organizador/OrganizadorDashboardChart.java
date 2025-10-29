package eventos.piura.dto.organizador;

import java.util.List;

public record OrganizadorDashboardChart(
        List<String> etiquetas,
        List<Integer> eventos,
        List<Integer> asistentes
) {
}
