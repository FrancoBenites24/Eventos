package eventos.piura.services;

import eventos.piura.model.Evento;
import eventos.piura.model.EventoImagen;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoImagenService {
    void asignarImagenes(Evento evento, List<MultipartFile> archivos);
    Optional<EventoImagen> obtenerPrimeraImagen(UUID eventoId);
    EventoImagen obtenerPorId(UUID imagenId);
    String construirUrl(EventoImagen imagen);
}
