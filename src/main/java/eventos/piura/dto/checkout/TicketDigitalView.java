package eventos.piura.dto.checkout;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketDigitalView(
        UUID boletoId,
        String codigo,
        String nombreTitular,
        String eventoTitulo,
        OffsetDateTime eventoFecha,
        String eventoLugar,
        String tipoNombre,
        String qrPayload,
        UUID ordenId,
        UUID ordenItemId,
        String boletaCodigo
) {
}
