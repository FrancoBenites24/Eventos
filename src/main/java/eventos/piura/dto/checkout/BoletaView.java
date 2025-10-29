package eventos.piura.dto.checkout;

import eventos.piura.model.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record BoletaView(
        String codigo,
        String clienteNombre,
        String clienteCorreo,
        MetodoPago metodoPago,
        String descripcionPago,
        List<BoletaItemView> items,
        int subtotalCentavos,
        int igvCentavos,
        int totalCentavos,
        String moneda,
        OffsetDateTime fechaPago
) {
    public BigDecimal subtotalMoneda() {
        return BigDecimal.valueOf(subtotalCentavos).movePointLeft(2);
    }

    public BigDecimal igvMoneda() {
        return BigDecimal.valueOf(igvCentavos).movePointLeft(2);
    }

    public BigDecimal totalMoneda() {
        return BigDecimal.valueOf(totalCentavos).movePointLeft(2);
    }
}
