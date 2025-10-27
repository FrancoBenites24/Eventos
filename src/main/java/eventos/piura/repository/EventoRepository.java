package eventos.piura.repository;

import eventos.piura.model.Evento;
import eventos.piura.model.enums.EstadoEvento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoRepository extends JpaRepository<Evento, UUID> {

    @EntityGraph(attributePaths = {"categoria", "tipos", "tipos.tipoEntrada"})
    List<Evento> findByEstadoAndFinEnGreaterThanEqualOrderByInicioEnAsc(EstadoEvento estado, OffsetDateTime fechaReferencia);

    @EntityGraph(attributePaths = {"categoria", "tipos", "tipos.tipoEntrada"})
    List<Evento> findByOrganizadorIdOrderByInicioEnDesc(UUID organizadorId);

    @EntityGraph(attributePaths = {"categoria", "tipos", "tipos.tipoEntrada"})
    List<Evento> findByOrganizadorIdAndInicioEnGreaterThanEqualOrderByInicioEnAsc(UUID organizadorId, OffsetDateTime fechaReferencia);

    @EntityGraph(attributePaths = {"categoria", "tipos", "tipos.tipoEntrada", "organizador"})
    Optional<Evento> findByIdAndEstado(UUID id, EstadoEvento estado);
}
