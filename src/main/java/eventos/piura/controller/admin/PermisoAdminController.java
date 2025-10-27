package eventos.piura.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/permisos")
public class PermisoAdminController {

    @GetMapping({"", "/"})
    public String listar() {
        // templates/admin/permisos.html
        return "admin/permisos";
    }

    @PostMapping({"", "/"})
    public String crearOActualizar(RedirectAttributes ra) {
        // TODO: servicio para crear/actualizar permiso
        ra.addFlashAttribute("toastSuccess", "Permiso guardado correctamente.");
        return "redirect:/admin/permisos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        // TODO: regla no-borrar-si-asignado-a-roles
        ra.addFlashAttribute("toastSuccess", "Permiso eliminado.");
        return "redirect:/admin/permisos";
    }
}
