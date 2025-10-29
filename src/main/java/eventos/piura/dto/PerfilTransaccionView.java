package eventos.piura.dto;

import java.util.UUID;

public record PerfilTransaccionView(
        UUID id,
        String tipo,
        String concepto,
        String fecha,
        String monto
) {
}
