package eventos.piura.controller.Admin;

import eventos.piura.model.Rol;
import eventos.piura.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolRepository rolRepository;

    // Listar roles
    @GetMapping
    public String listarRoles(Model model) {
        List<Rol> roles = rolRepository.findAll();
        model.addAttribute("roles", roles);
        model.addAttribute("content", "admin/roles");
        model.addAttribute("title", "Gestión de Roles");

        return "admin/layout";
    }

    // Mostrar formulario para crear
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("rol", new Rol());
        model.addAttribute("content", "admin/rol-form");
        model.addAttribute("title", "Crear Rol");

        return "admin/layout";
    }

    // Guardar nuevo rol
    @PostMapping("/guardar")
    public String guardarRol(@ModelAttribute Rol rol) {
        rolRepository.save(rol);
        return "redirect:/admin/roles";
    }

    // Editar
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Rol rol = rolRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
        model.addAttribute("rol", rol);
        model.addAttribute("content", "admin/rol-form");
        model.addAttribute("title", "Editar Rol");

        return "admin/layout";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarRol(@PathVariable Long id, @ModelAttribute Rol rol) {
        rol.setId(id);
        rolRepository.save(rol);
        return "redirect:/admin/roles";
    }

    // Eliminar
    @GetMapping("/eliminar/{id}")
    public String eliminarRol(@PathVariable Long id) {
        rolRepository.deleteById(id);
        return "redirect:/admin/roles";
    }
}
