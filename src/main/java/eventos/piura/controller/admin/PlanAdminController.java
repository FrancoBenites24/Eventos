package eventos.piura.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/planes")
public class PlanAdminController {

    @GetMapping({"", "/"})
    public String listar() {
        // templates/admin/planes.html
        return "admin/planes";
    }

    @PostMapping({"", "/"})
    public String crearOActualizar(RedirectAttributes ra) {
        // TODO: servicio crear/actualizar plan (validar JSON de características, unicidad de código)
        ra.addFlashAttribute("toastSuccess", "Plan guardado correctamente.");
        return "redirect:/admin/planes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        // TODO: no-borrar-si-tiene-suscripciones
        ra.addFlashAttribute("toastSuccess", "Plan eliminado.");
        return "redirect:/admin/planes";
    }
}
