package eventos.piura.dto.carrito;

import java.math.BigDecimal;
import java.util.List;

public record CarritoResumenView(
        List<CarritoItemView> items,
        int totalCentavos,
        int totalCantidad
) {
    public BigDecimal totalMoneda() {
        return BigDecimal.valueOf(totalCentavos).movePointLeft(2);
    }
}
