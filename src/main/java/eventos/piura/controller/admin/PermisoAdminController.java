package eventos.piura.controller.admin;

import eventos.piura.model.Permiso;
import eventos.piura.service.PermisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/permisos")
@RequiredArgsConstructor
public class PermisoAdminController {

    private final PermisoService permisoService;

    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("permisos", permisoService.listar());
        return "admin/permisos";
    }

    @PostMapping({"", "/"})
    public String crearOActualizar(@ModelAttribute Permiso permiso, RedirectAttributes ra) {
        permisoService.guardar(permiso);
        ra.addFlashAttribute("toastSuccess", "Permiso guardado correctamente.");
        return "redirect:/admin/permisos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable UUID id, RedirectAttributes ra) {
        if (permisoService.eliminar(id)) {
            ra.addFlashAttribute("toastSuccess", "Permiso eliminado.");
        } else {
            ra.addFlashAttribute("toastError", "No se pudo eliminar el permiso.");
        }
        return "redirect:/admin/permisos";
    }
}
