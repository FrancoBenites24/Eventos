package eventos.piura.dto.carrito;

import java.math.BigDecimal;
import java.util.UUID;

public record CarritoItemView(
        UUID eventoId,
        UUID entradaTipoId,
        String titulo,
        String tipoNombre,
        int cantidad,
        int precioCentavos,
        int subtotalCentavos
) {
    public BigDecimal precioMoneda() {
        return BigDecimal.valueOf(precioCentavos).movePointLeft(2);
    }

    public BigDecimal subtotalMoneda() {
        return BigDecimal.valueOf(subtotalCentavos).movePointLeft(2);
    }
}
