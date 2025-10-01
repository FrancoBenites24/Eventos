package eventos.piura.Seguridad.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    @GetMapping
    public String listarUsuarios(Model model) {
        // Aquí luego irán los usuarios desde la BD
        model.addAttribute("usuarios", java.util.Collections.emptyList());
        return "admin/usuarios";
    }

    /*@GetMapping("/crear")
    public String mostrarFormularioCrear() {
        return "admin/usuario-form";
    }

    @PostMapping("/crear")
    public String crearUsuario() {
        // Guardar usuario (luego)
        return "redirect:/admin/usuarios";
    }
    @GetMapping("/edit/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        // Buscar usuario por id
        return "admin/usuario-form";
    }

    @PostMapping("/edit/{id}")
    public String actualizarUsuario(@PathVariable Long id) {
        // Actualizar usuario
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/delete/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        // Eliminar usuario
        return "redirect:/admin/usuarios";
    }*/

}
