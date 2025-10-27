package eventos.piura.dto.checkout;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckoutItemView(
        UUID entradaTipoId,
        UUID eventoId,
        String eventoTitulo,
        String eventoLugar,
        OffsetDateTime eventoFecha,
        String tipoNombre,
        int cantidad,
        int precioCentavos,
        int subtotalCentavos
) {
}
