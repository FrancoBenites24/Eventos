package eventos.piura.services;

import eventos.piura.dto.EventoDetalleView;

import java.util.UUID;

public interface EventoDetalleService {
    EventoDetalleView obtenerDetallePublicado(UUID eventoId);
}
