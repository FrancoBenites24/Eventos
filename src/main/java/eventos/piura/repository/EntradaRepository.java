package eventos.piura.repository;

import eventos.piura.model.Entrada;
import eventos.piura.model.enums.EstadoEntrada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EntradaRepository extends JpaRepository<Entrada, UUID> {

    long countByEventoId(UUID eventoId);

    long countByEventoIdAndEstadoNot(UUID eventoId, EstadoEntrada estado);
}
