package eventos.piura.dto.checkout;

import eventos.piura.model.enums.MetodoPago;

import java.util.UUID;

public record MetodoPagoGuardadoView(
        UUID id,
        MetodoPago tipo,
        String etiqueta,
        String descripcion,
        String detalle
) {
}
