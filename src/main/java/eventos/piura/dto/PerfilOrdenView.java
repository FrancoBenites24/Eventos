package eventos.piura.dto;

import java.util.UUID;

public record PerfilOrdenView(
        UUID id,
        String tituloEvento,
        String fecha,
        String total,
        String estadoEtiqueta,
        String estadoClase,
        String metodoPago
) {
}
