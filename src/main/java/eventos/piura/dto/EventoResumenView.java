package eventos.piura.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class EventoResumenView {
    private final UUID eventoId;
    private final UUID entradaTipoId;
    private final String titulo;
    private final OffsetDateTime fecha;
    private final String lugar;
    private final Integer precioCentavos;
    private final String categoria;
    private final String imagenUrl;
}
