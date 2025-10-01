package eventos.piura.services;

import eventos.piura.model.Evento;
import eventos.piura.model.Ticket;
import eventos.piura.repository.EventoRepository;
import eventos.piura.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventoService {

    private static final Logger logger = LoggerFactory.getLogger(EventoService.class);
    
    @Autowired
    private EventoRepository eventoRepository;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Transactional(readOnly = true)
    public List<Evento> obtenerProximosEventos(Long usuarioId) {
        try {
            logger.info("Buscando próximos eventos para el usuario ID: {}", usuarioId);
            List<Ticket> tickets = ticketRepository.findByUsuarioIdAndTipoTicket_Evento_FechaInicioAfter(usuarioId, LocalDateTime.now());
            logger.info("Se encontraron {} tickets para eventos futuros", tickets.size());
            
            List<Evento> eventos = tickets.stream()
                    .map(ticket -> {
                        try {
                            return ticket.getTipoTicket().getEvento();
                        } catch (Exception e) {
                            logger.error("Error al obtener evento de ticket ID {}: {}", ticket.getId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(evento -> evento != null)
                    .distinct()
                    .collect(Collectors.toList());
            
            logger.info("Se encontraron {} próximos eventos únicos para el usuario ID: {}", eventos.size(), usuarioId);
            return eventos;
        } catch (Exception e) {
            logger.error("Error al buscar próximos eventos para el usuario ID: {}", usuarioId, e);
            return Collections.emptyList();
        }
    }
}