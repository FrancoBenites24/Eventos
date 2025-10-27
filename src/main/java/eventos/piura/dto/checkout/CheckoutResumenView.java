package eventos.piura.dto.checkout;

import eventos.piura.model.enums.MetodoPago;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutResumenView(
        List<CheckoutItemView> items,
        int subtotalCentavos,
        int igvCentavos,
        int totalCentavos,
        String moneda,
        List<MetodoPago> metodos,
        Integer saldoBilleteraCentavos
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
