package eventos.piura.repository;

import eventos.piura.model.EventoEntradaTipo;
import eventos.piura.model.enums.EstadoEvento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventoEntradaTipoRepository extends JpaRepository<EventoEntradaTipo, UUID> {

    @EntityGraph(attributePaths = {"evento", "evento.organizador", "evento.categoria"})
    Optional<EventoEntradaTipo> findByIdAndEventoEstado(UUID id, EstadoEvento estado);
}
