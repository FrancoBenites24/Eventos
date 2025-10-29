package eventos.piura.repository;

import eventos.piura.model.Orden;
import eventos.piura.model.enums.EstadoOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrdenRepository extends JpaRepository<Orden, UUID> {

    @Query("select coalesce(sum(o.totalCentavos), 0) from Orden o " +
            "where o.evento.organizador.id = :organizadorId and o.estado = :estado")
    long sumTotalCentavosByOrganizadorIdAndEstado(@Param("organizadorId") UUID organizadorId,
                                                  @Param("estado") EstadoOrden estado);

    @Query("select coalesce(sum(o.totalCentavos), 0) from Orden o " +
            "where o.evento.id = :eventoId and o.estado = :estado")
    long sumTotalCentavosByEventoIdAndEstado(@Param("eventoId") UUID eventoId,
                                             @Param("estado") EstadoOrden estado);

    List<Orden> findTop5ByCompradorIdOrderByCreadoEnDesc(UUID compradorId);

    long countByCompradorId(UUID compradorId);

    @Query("select coalesce(sum(o.totalCentavos), 0) from Orden o " +
            "where o.comprador.id = :compradorId and o.estado = :estado")
    long sumTotalCentavosByCompradorIdAndEstado(@Param("compradorId") UUID compradorId,
                                                @Param("estado") EstadoOrden estado);
}
