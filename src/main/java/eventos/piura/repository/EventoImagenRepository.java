package eventos.piura.repository;

import eventos.piura.model.EventoImagen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventoImagenRepository extends JpaRepository<EventoImagen, UUID> {
    Optional<EventoImagen> findFirstByEventoIdOrderByOrdenAsc(UUID eventoId);
}
