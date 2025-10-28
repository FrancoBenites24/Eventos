package eventos.piura.dto;

import java.util.UUID;

public record PerfilEventoView(
        UUID eventoId,
        String titulo,
        String categoria,
        String fecha,
        String checkIn,
        String imgUrl
) {
}
