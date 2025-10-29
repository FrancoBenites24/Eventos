package eventos.piura.controller;

import eventos.piura.dto.organizador.NuevoEventoForm;
import eventos.piura.services.OrganizadorPanelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/organizador")
@RequiredArgsConstructor
public class OrganizadorController {

    private final OrganizadorPanelService panelService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        return panelService.mostrarDashboard(model, authentication);
    }

    @GetMapping("/eventos")
    public String eventos(@RequestParam(value = "busqueda", required = false) String busqueda,
                          @RequestParam(value = "estado", required = false) String estado,
                          @RequestParam(value = "tipo", required = false) String tipo,
                          Model model,
                          Authentication authentication) {
        return panelService.listarEventos(busqueda, estado, tipo, model, authentication);
    }

    @GetMapping("/eventos/nuevo")
    public String nuevoEvento(Model model, Authentication authentication) {
        return panelService.prepararNuevoEvento(model, authentication);
    }

    @PostMapping("/eventos")
    public String crearEvento(@ModelAttribute("form") NuevoEventoForm form,
                              BindingResult bindingResult,
                              Model model,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        return panelService.crearEvento(form, bindingResult, model, authentication, redirectAttributes);
    }

    @GetMapping("/eventos/{eventoId}/editar")
    public String editarEvento(@PathVariable UUID eventoId,
                               Model model,
                               Authentication authentication) {
        return panelService.prepararEdicionEvento(eventoId, model, authentication);
    }

    @PutMapping("/eventos/{eventoId}")
    public String actualizarEvento(@PathVariable UUID eventoId,
                                   @ModelAttribute("form") NuevoEventoForm form,
                                   BindingResult bindingResult,
                                   Model model,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        return panelService.actualizarEvento(eventoId, form, bindingResult, model, authentication, redirectAttributes);
    }

    @DeleteMapping("/eventos/{eventoId}")
    public String eliminarEvento(@PathVariable UUID eventoId,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        return panelService.eliminarEvento(eventoId, authentication, redirectAttributes);
    }

    @GetMapping("/configuracion")
    public String configuracion(@RequestParam(name = "tab", required = false) String tab,
                                Model model,
                                Authentication authentication) {
        return panelService.mostrarConfiguracion(tab, model, authentication);
    }

    @GetMapping("/analitica")
    public String analitica() {
        return panelService.redirigirAnalitica();
    }
}
