package eventos.piura.controller.admin;

import eventos.piura.model.Usuario;
import eventos.piura.service.UsuarioService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    private final UsuarioService usuarioService;

    public UsuarioAdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "admin/usuarios";
    }

    @PostMapping({"", "/"})
    public String crearOActualizar(@ModelAttribute Usuario usuario, RedirectAttributes ra) {
        usuarioService.guardar(usuario);
        ra.addFlashAttribute("toastSuccess", "Usuario guardado correctamente.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable UUID id, RedirectAttributes ra) {
        usuarioService.eliminar(id);
        ra.addFlashAttribute("toastSuccess", "Usuario eliminado.");
        return "redirect:/admin/usuarios";
    }
}
