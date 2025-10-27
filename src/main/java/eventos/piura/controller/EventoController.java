package eventos.piura.controller;

import eventos.piura.dto.CatalogoEventosView;
import eventos.piura.dto.EventoDetalleView;
import eventos.piura.services.EventoCatalogService;
import eventos.piura.services.EventoDetalleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
public class EventoController {

    private final EventoCatalogService eventoCatalogService;
    private final EventoDetalleService eventoDetalleService;

    @GetMapping("/eventos")
    public String eventos(Model model) {
        CatalogoEventosView catalogo = eventoCatalogService.construirCatalogoEventos();
        model.addAttribute("destacado", catalogo.destacado());
        model.addAttribute("porCat", catalogo.porCategoria());
        return "eventos";
    }

    @GetMapping("/eventos/{eventoId}")
    public String detalleEvento(@PathVariable UUID eventoId, Model model) {
        EventoDetalleView detalle = eventoDetalleService.obtenerDetallePublicado(eventoId);
        List<String> imagenes = detalle.getEvento().getImagenes();
        String imagenPrincipal = imagenes.isEmpty() ? "/img/placeholder-event.jpg" : imagenes.get(0);
        String ubicacion = Stream.of(
                        detalle.getEvento().getDireccion(),
                        detalle.getEvento().getDistrito(),
                        detalle.getEvento().getProvincia(),
                        detalle.getEvento().getDepartamento(),
                        detalle.getEvento().getPais())
                .filter(valor -> valor != null && !valor.isBlank())
                .collect(Collectors.joining(", "));
        if (ubicacion.isBlank()) {
            ubicacion = "Ubicacion por definir";
        }

        String organizadorNombre = Optional.ofNullable(detalle.getOrganizador())
                .map(org -> Stream.of(org.getNombre(), org.getApellido())
                        .filter(valor -> valor != null && !valor.isBlank())
                        .collect(Collectors.joining(" ")))
                .filter(nombre -> !nombre.isBlank())
                .orElse("Organizador por definir");

        int precioMinimo = detalle.getTickets().stream()
                .map(EventoDetalleView.TicketInfo::getPrecioCentavos)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(0);
        boolean eventoGratuito = precioMinimo == 0;

        model.addAttribute("detalle", detalle);
        model.addAttribute("evento", detalle.getEvento());
        model.addAttribute("organizador", detalle.getOrganizador());
        model.addAttribute("tiposTicket", detalle.getTickets());
        model.addAttribute("imagenes", imagenes);
        model.addAttribute("imagenPrincipal", imagenPrincipal);
        model.addAttribute("creadoEn", detalle.getEvento().getCreadoEn());
        model.addAttribute("ubicacion", ubicacion);
        model.addAttribute("organizadorNombre", organizadorNombre);
        model.addAttribute("precioMinimo", precioMinimo);
        model.addAttribute("eventoGratuito", eventoGratuito);

        return "detalle-evento";
    }
}
