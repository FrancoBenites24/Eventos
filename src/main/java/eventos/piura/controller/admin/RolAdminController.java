package eventos.piura.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/roles")
public class RolAdminController {

    @GetMapping({"", "/"})
    public String listar() {
        // templates/admin/roles.html
        return "admin/roles";
    }

    @PostMapping({"", "/"})
    public String crearOActualizar(RedirectAttributes ra) {
        // TODO: servicio para crear/actualizar rol + asignar permisos
        ra.addFlashAttribute("toastSuccess", "Rol guardado correctamente.");
        return "redirect:/admin/roles";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        // TODO: regla no-borrar-si-tiene-usuarios
        ra.addFlashAttribute("toastSuccess", "Rol eliminado.");
        return "redirect:/admin/roles";
    }
}
