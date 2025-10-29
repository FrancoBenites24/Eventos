package eventos.piura.repository;

import eventos.piura.model.Entrada;
import eventos.piura.model.enums.EstadoEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface EntradaRepository extends JpaRepository<Entrada, UUID> {

    long countByEventoId(UUID eventoId);

    long countByEventoIdAndEstadoNot(UUID eventoId, EstadoEntrada estado);

    @Query("""
            select count(distinct e.evento.id) from Entrada e
            where e.orden.comprador.id = :compradorId
              and e.estado = :estado
            """)
    long countEventosAsistidos(@Param("compradorId") UUID compradorId,
                               @Param("estado") EstadoEntrada estado);

    @Query("""
            select count(e) from Entrada e
            where e.orden.comprador.id = :compradorId
              and e.estado = :estado
              and e.evento.inicioEn >= :desde
            """)
    long countEntradasActivas(@Param("compradorId") UUID compradorId,
                              @Param("estado") EstadoEntrada estado,
                              @Param("desde") OffsetDateTime desde);

    @Query("""
            select e from Entrada e
              join fetch e.evento ev
              left join fetch ev.categoria
            where e.orden.comprador.id = :compradorId
              and e.estado = :estado
            order by coalesce(e.usadaEn, ev.inicioEn) desc
            """)
    List<Entrada> findEntradasPorEstado(@Param("compradorId") UUID compradorId,
                                        @Param("estado") EstadoEntrada estado);

    @Query("""
            select e from Entrada e
              join fetch e.evento ev
              left join fetch ev.categoria
            where e.orden.comprador.id = :compradorId
              and e.estado = :estado
              and ev.inicioEn >= :desde
            order by ev.inicioEn asc
            """)
    List<Entrada> findEntradasUpcoming(@Param("compradorId") UUID compradorId,
                                       @Param("estado") EstadoEntrada estado,
                                       @Param("desde") OffsetDateTime desde);
}
