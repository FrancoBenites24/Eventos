package eventos.piura.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/tipos-entrada")
public class TipoEntradaAdminController {

    @GetMapping({"", "/"})
    public String listar() {
        // templates/admin/tipos-entrada.html
        return "admin/tipos-entrada";
    }

    @PostMapping({"", "/"})
    public String crearOActualizar(RedirectAttributes ra) {
        // TODO: servicio crear/actualizar tipo de entrada
        ra.addFlashAttribute("toastSuccess", "Tipo de entrada guardado.");
        return "redirect:/admin/tipos-entrada";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        // TODO: regla no-borrar-si-lo-usan-eventos
        ra.addFlashAttribute("toastSuccess", "Tipo de entrada eliminado.");
        return "redirect:/admin/tipos-entrada";
    }
}
