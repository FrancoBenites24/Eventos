package eventos.piura.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/suscripciones")
public class SuscripcionAdminController {

    @GetMapping({"", "/"})
    public String listar() {
        // templates/admin/Suscripciones.html (ojo mayúscula)
        return "admin/suscripciones";
    }

    @PostMapping({"", "/"})
    public String crearOActualizar(RedirectAttributes ra) {
        // TODO: crear/actualizar suscripción (validar fechas, estado, monto)
        ra.addFlashAttribute("toastSuccess", "Suscripción guardada.");
        return "redirect:/admin/suscripciones";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        // TODO: regla de negocio si aplica
        ra.addFlashAttribute("toastSuccess", "Suscripción eliminada.");
        return "redirect:/admin/suscripciones";
    }
}
