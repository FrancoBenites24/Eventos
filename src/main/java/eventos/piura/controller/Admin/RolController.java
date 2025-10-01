package eventos.piura.controller.Admin;

import eventos.piura.model.Rol;
import eventos.piura.model.Permiso;
import eventos.piura.model.RolPermiso;
import eventos.piura.repository.RolRepository;
import eventos.piura.repository.PermisoRepository;
import eventos.piura.repository.RolPermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final RolPermisoRepository rolPermisoRepository;

   @GetMapping
public String listarRoles(Model model) {
    List<Rol> roles = rolRepository.findAll();
    List<Permiso> permisos = permisoRepository.findAll();

    // Mapeo rolId -> lista de permisos asignados
Map<Long, List<Permiso>> permisosPorRol = new HashMap<>();
for (Rol r : roles) {
    List<Permiso> permisosAsignados = rolPermisoRepository.findByRolId(r.getId())
                                                          .stream()
                                                          .map(RolPermiso::getPermiso)
                                                          .toList();
    permisosPorRol.put(r.getId(), permisosAsignados);
}


    model.addAttribute("roles", roles);
    model.addAttribute("permisos", permisos);
    model.addAttribute("permisosPorRol", permisosPorRol); // 👈 clave

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

    // 👉 Nuevo: asignar permisos a un rol
    @PostMapping("/asignar-permisos/{id}")
    public String asignarPermisos(@PathVariable Long id,
                                  @RequestParam(value = "permisos", required = false) List<Long> permisosIds) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        // Eliminar permisos anteriores
        rolPermisoRepository.deleteByRolId(rol.getId());

        // Guardar nuevas asignaciones
        if (permisosIds != null) {
            for (Long permisoId : permisosIds) {
                Permiso permiso = permisoRepository.findById(permisoId)
                        .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado"));

                RolPermiso rp = new RolPermiso();
                rp.setRol(rol);
                rp.setPermiso(permiso);
                rolPermisoRepository.save(rp);
            }
        }

        return "redirect:/admin/roles";
    }
}
