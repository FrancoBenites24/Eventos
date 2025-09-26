package eventos.piura.repository;

import eventos.piura.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("SELECT t FROM Ticket t WHERE t.usuario.id = :usuarioId AND t.tipoTicket.evento.fechaInicio > :fechaActual")
    List<Ticket> findByUsuarioIdAndTipoTicket_Evento_FechaInicioAfter(
            @Param("usuarioId") Long usuarioId, 
            @Param("fechaActual") LocalDateTime fechaActual);
}