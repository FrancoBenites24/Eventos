package eventos.piura.controller.Admin;

import eventos.piura.model.Permiso;
import eventos.piura.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoRepository permisoRepository;

    // Listar permisos
    @GetMapping
    public String listarPermisos(Model model) {
        List<Permiso> permisos = permisoRepository.findAll();
        model.addAttribute("permisos", permisos);
        model.addAttribute("content", "admin/permisos");
        model.addAttribute("title", "Gestión de Permisos");

        return "admin/layout";
    }

    // Mostrar formulario crear
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("permiso", new Permiso());
        model.addAttribute("content", "admin/permiso-form");
        model.addAttribute("title", "Crear Permiso");

        return "admin/layout";
    }

    // Guardar permiso
    @PostMapping("/guardar")
    public String guardarPermiso(@ModelAttribute Permiso permiso) {
        permisoRepository.save(permiso);
        return "redirect:/admin/permisos";
    }

    // Editar
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado"));
        model.addAttribute("permiso", permiso);
        model.addAttribute("content", "admin/permiso-form");
        model.addAttribute("title", "Editar Permiso");

        return "admin/layout";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarPermiso(@PathVariable Long id, @ModelAttribute Permiso permiso) {
        permiso.setId(id);
        permisoRepository.save(permiso);
        return "redirect:/admin/permisos";
    }

    // Eliminar
    @GetMapping("/eliminar/{id}")
    public String eliminarPermiso(@PathVariable Long id) {
        permisoRepository.deleteById(id);
        return "redirect:/admin/permisos";
    }
}
