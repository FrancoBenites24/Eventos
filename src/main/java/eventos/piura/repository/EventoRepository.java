package eventos.piura.repository;

import eventos.piura.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface EventoRepository extends JpaRepository<Evento, UUID> {
    long countByCategoriaId(UUID categoriaId);
    boolean existsByCategoriaId(UUID categoriaId);

    @Query("select e.categoria.id as categoriaId, count(e) as total " +
           "from Evento e where e.categoria.id in :categoriaIds group by e.categoria.id")
    List<EventoCategoriaConteo> contarEventosPorCategoriaIds(@Param("categoriaIds") Collection<UUID> categoriaIds);

    interface EventoCategoriaConteo {
        UUID getCategoriaId();
        long getTotal();
    }
}
