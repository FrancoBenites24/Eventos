package eventos.piura.dto.checkout;

import java.util.UUID;

public record BoletaItemView(
        UUID eventoId,
        String eventoTitulo,
        String tipoNombre,
        int cantidad,
        int precioCentavos,
        int subtotalCentavos
) {
}
