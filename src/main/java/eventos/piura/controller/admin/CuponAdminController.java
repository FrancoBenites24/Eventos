package eventos.piura.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/cupones")
public class CuponAdminController {

    @GetMapping({"", "/"})
    public String listar() {
        // templates/admin/cupones.html
        return "admin/cupones";
    }

    @PostMapping({"", "/"})
    public String crearOActualizar(RedirectAttributes ra) {
        // TODO: crear/actualizar cupón (validar rango de fechas y %)
        ra.addFlashAttribute("toastSuccess", "Cupón guardado.");
        return "redirect:/admin/cupones";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        // TODO: reglas si el cupón ya fue usado
        ra.addFlashAttribute("toastSuccess", "Cupón eliminado.");
        return "redirect:/admin/cupones";
    }
}
