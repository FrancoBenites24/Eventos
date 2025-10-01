package eventos.piura.Seguridad.Controller;

import eventos.piura.Seguridad.Model.Rol;
import eventos.piura.Seguridad.Repository.PermisoRepository;
import eventos.piura.Seguridad.Repository.RolRepository;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/roles")
public class RolController {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    public RolController(RolRepository rolRepository, PermisoRepository permisoRepository) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
    }

    @GetMapping
    public String listarRoles(Model model) {
        model.addAttribute("roles", rolRepository.findAll());
        return "admin/roles";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("rol", new Rol());
        model.addAttribute("permisos", permisoRepository.findAll());
        return "admin/rol-form";
    }

    @PostMapping("/crear")
public String crearRol(@ModelAttribute Rol rol,
                       @RequestParam(value = "permisosIds", required = false) List<Long> permisosIds) {
    if (permisosIds != null) {
        rol.setPermisos(new HashSet<>(permisoRepository.findAllById(permisosIds)));
    }
    rolRepository.save(rol);
    return "redirect:/admin/roles";
}

    @GetMapping("/edit/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Rol rol = rolRepository.findById(id).orElseThrow();
        model.addAttribute("rol", rol);
        model.addAttribute("permisos", permisoRepository.findAll());
        return "admin/rol-form";
    }

    @PostMapping("/edit/{id}")
public String actualizarRol(@PathVariable Long id,
                            @ModelAttribute Rol rol,
                            @RequestParam(value = "permisosIds", required = false) List<Long> permisosIds) {
    rol.setId(id);
    if (permisosIds != null) {
        rol.setPermisos(new HashSet<>(permisoRepository.findAllById(permisosIds)));
    } else {
        rol.setPermisos(new HashSet<>());
    }
    rolRepository.save(rol);
    return "redirect:/admin/roles";
}

    @GetMapping("/delete/{id}")
    public String eliminarRol(@PathVariable Long id) {
        rolRepository.deleteById(id);
        return "redirect:/admin/roles";
    }
}
