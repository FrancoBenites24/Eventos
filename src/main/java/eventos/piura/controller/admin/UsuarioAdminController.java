package eventos.piura.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    @GetMapping({"", "/"})
    public String listar() {
        // templates/admin/usuarios.html
        return "admin/usuarios";
    }

    @PostMapping({"", "/"})
    public String crearOActualizar(
            // TODO: reemplazar por DTO @ModelAttribute @Validated
            RedirectAttributes ra
    ) {
        // TODO: llamar a servicio para crear/actualizar
        ra.addFlashAttribute("toastSuccess", "Usuario guardado correctamente.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        // TODO: servicio.delete(id) con regla "no borrar en uso"
        ra.addFlashAttribute("toastSuccess", "Usuario eliminado.");
        return "redirect:/admin/usuarios";
    }
}
