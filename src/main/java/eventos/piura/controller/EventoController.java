package eventos.piura.controller;

import eventos.piura.dto.CatalogoEventosView;
import eventos.piura.services.EventoCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class EventoController {

    private final EventoCatalogService eventoCatalogService;

    @GetMapping("/eventos")
    public String eventos(Model model) {
        CatalogoEventosView catalogo = eventoCatalogService.construirCatalogoEventos();
        model.addAttribute("destacado", catalogo.destacado());
        model.addAttribute("porCat", catalogo.porCategoria());
        return "eventos";
    }
}
