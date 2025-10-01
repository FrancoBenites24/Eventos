package eventos.piura.Seguridad.Controller;

import eventos.piura.Seguridad.Model.Permiso;
import eventos.piura.Seguridad.Repository.PermisoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/permisos")
public class PermisoController {

    private final PermisoRepository permisoRepository;

    public PermisoController(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    @GetMapping
    public String listarPermisos(Model model) {
        model.addAttribute("permisos", permisoRepository.findAll());
        return "admin/permisos";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("permiso", new Permiso());
        return "admin/permiso-form";
    }

    @PostMapping("/crear")
    public String crearPermiso(@ModelAttribute Permiso permiso) {
        permisoRepository.save(permiso);
        return "redirect:/admin/permisos";
    }

    @GetMapping("/edit/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("permiso", permisoRepository.findById(id).orElseThrow());
        return "admin/permiso-form";
    }

    @PostMapping("/edit/{id}")
    public String actualizarPermiso(@PathVariable Long id, @ModelAttribute Permiso permiso) {
        permiso.setId(id);
        permisoRepository.save(permiso);
        return "redirect:/admin/permisos";
    }

    @GetMapping("/delete/{id}")
    public String eliminarPermiso(@PathVariable Long id) {
        permisoRepository.deleteById(id);
        return "redirect:/admin/permisos";
    }
}
