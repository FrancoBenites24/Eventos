package eventos.piura.services.impl;

import eventos.piura.dto.EventoDetalleView;
import eventos.piura.model.Evento;
import eventos.piura.model.EventoEntradaTipo;
import eventos.piura.model.EventoImagen;
import eventos.piura.model.Usuario;
import eventos.piura.model.enums.EstadoEvento;
import eventos.piura.repository.EventoRepository;
import eventos.piura.services.EventoDetalleService;
import eventos.piura.services.EventoImagenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventoDetalleServiceImpl implements EventoDetalleService {

    private static final String IMAGEN_POR_DEFECTO = "/img/placeholder-event.png";

    private final EventoRepository eventoRepository;
    private final EventoImagenService eventoImagenService;

    @Override
    @Transactional(readOnly = true)
    public EventoDetalleView obtenerDetallePublicado(UUID eventoId) {
        Evento evento = eventoRepository.findByIdAndEstado(eventoId, EstadoEvento.PUBLICADO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no disponible"));

        evento.getImagenes().size();
        evento.getTipos().size();

        List<String> imagenes = construirImagenes(evento);
        EventoDetalleView.EventoInfo eventoInfo = construirEventoInfo(evento, imagenes);
        EventoDetalleView.OrganizadorInfo organizadorInfo = construirOrganizadorInfo(evento.getOrganizador());
        List<EventoDetalleView.TicketInfo> tickets = construirTickets(evento.getTipos());

        return EventoDetalleView.builder()
                .evento(eventoInfo)
                .organizador(organizadorInfo)
                .tickets(tickets)
                .build();
    }

    private List<String> construirImagenes(Evento evento) {
        List<String> urls = evento.getImagenes().stream()
                .sorted(Comparator.comparing(
                        EventoImagen::getOrden,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(eventoImagenService::construirUrl)
                .collect(Collectors.toList());

        if (urls.isEmpty()) {
            urls.add(IMAGEN_POR_DEFECTO);
        }
        return urls;
    }

    private EventoDetalleView.EventoInfo construirEventoInfo(Evento evento, List<String> imagenes) {
        return EventoDetalleView.EventoInfo.builder()
                .id(evento.getId())
                .titulo(evento.getTitulo())
                .descripcion(evento.getDescripcion())
                .categoria(evento.getCategoria() != null ? evento.getCategoria().getNombre() : null)
                .inicio(evento.getInicioEn())
                .fin(evento.getFinEn())
                .creadoEn(evento.getCreadoEn())
                .direccion(evento.getDireccion())
                .distrito(evento.getDistrito())
                .provincia(evento.getProvincia())
                .departamento(evento.getDepartamento())
                .pais(evento.getPais())
                .latitud(evento.getLatitud())
                .longitud(evento.getLongitud())
                .imagenes(imagenes)
                .build();
    }

    private EventoDetalleView.OrganizadorInfo construirOrganizadorInfo(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return EventoDetalleView.OrganizadorInfo.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .build();
    }

    private List<EventoDetalleView.TicketInfo> construirTickets(List<EventoEntradaTipo> tipos) {
        return tipos.stream()
                .sorted(Comparator.comparing(EventoEntradaTipo::getPrecioCentavos, Comparator.nullsLast(Integer::compareTo)))
                .map(tipo -> EventoDetalleView.TicketInfo.builder()
                        .id(tipo.getId())
                        .nombre(tipo.getNombreVisible())
                        .precioCentavos(tipo.getPrecioCentavos())
                        .cupoTotal(tipo.getCupoTotal())
                        .build())
                .toList();
    }
}
