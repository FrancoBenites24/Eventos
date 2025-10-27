package eventos.piura.services.impl;

import eventos.piura.dto.CatalogoEventosView;
import eventos.piura.dto.EventoResumenView;
import eventos.piura.model.Evento;
import eventos.piura.model.EventoEntradaTipo;
import eventos.piura.model.enums.EstadoEvento;
import eventos.piura.repository.EventoRepository;
import eventos.piura.services.EventoCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EventoCatalogServiceImpl implements EventoCatalogService {

    private static final String CATEGORIA_POR_DEFECTO = "Otros";
    private static final String IMAGEN_POR_DEFECTO = "/img/placeholder-event.jpg";
    private static final String LUGAR_POR_DEFECTO = "Ubicacion por confirmar";

    private final EventoRepository eventoRepository;

    @Override
    @Transactional(readOnly = true)
    public CatalogoEventosView construirCatalogoEventos() {
        List<EventoResumenView> eventos = eventoRepository
                .findByEstadoAndFinEnGreaterThanEqualOrderByInicioEnAsc(
                        EstadoEvento.PUBLICADO,
                        OffsetDateTime.now()
                )
                .stream()
                .map(this::mapearAResumen)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (eventos.isEmpty()) {
            return CatalogoEventosView.vacio();
        }

        EventoResumenView destacado = eventos.get(0);

        Map<String, List<EventoResumenView>> porCategoria = eventos.stream()
                .collect(Collectors.groupingBy(
                        EventoResumenView::getCategoria,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return new CatalogoEventosView(destacado, porCategoria);
    }

    private EventoResumenView mapearAResumen(Evento evento) {
        Optional<EventoEntradaTipo> tipoPrincipal = evento.getTipos().stream()
                .filter(tipo -> tipo.getPrecioCentavos() != null)
                .min(Comparator.comparingInt(EventoEntradaTipo::getPrecioCentavos));

        if (tipoPrincipal.isEmpty()) {
            return null;
        }

        EventoEntradaTipo tipo = tipoPrincipal.get();

        return EventoResumenView.builder()
                .eventoId(evento.getId())
                .entradaTipoId(tipo.getId())
                .titulo(evento.getTitulo())
                .fecha(evento.getInicioEn())
                .lugar(resolverLugar(evento))
                .precioCentavos(tipo.getPrecioCentavos())
                .categoria(resolverCategoria(evento))
                .imagenUrl(resolverImagen(evento))
                .build();
    }

    private String resolverCategoria(Evento evento) {
        return Optional.ofNullable(evento.getCategoria())
                .map(cat -> cat.getNombre() == null || cat.getNombre().isBlank() ? CATEGORIA_POR_DEFECTO : cat.getNombre())
                .orElse(CATEGORIA_POR_DEFECTO);
    }

    private String resolverImagen(Evento evento) {
        return IMAGEN_POR_DEFECTO;
    }

    private String resolverLugar(Evento evento) {
        String ubicacion = Stream.of(evento.getDistrito(), evento.getProvincia(), evento.getDepartamento())
                .filter(valor -> valor != null && !valor.isBlank())
                .collect(Collectors.joining(", "));

        if (!ubicacion.isBlank()) {
            return ubicacion;
        }

        if (evento.getDireccion() != null && !evento.getDireccion().isBlank()) {
            return evento.getDireccion();
        }

        return LUGAR_POR_DEFECTO;
    }
}
