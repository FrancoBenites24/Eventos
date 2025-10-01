package eventos.piura.controller.Admin;

import eventos.piura.model.Rol;
import eventos.piura.model.Usuario;
import eventos.piura.model.UsuarioRol;
import eventos.piura.repository.RolRepository;
import eventos.piura.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioAdminController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    @GetMapping
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Rol> roles = rolRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", roles);
        model.addAttribute("content", "admin/usuarios");
        model.addAttribute("title", "Gestión de Usuarios");
        return "admin/layout";
    }

    @PostMapping("/asignar-roles/{id}")
public String asignarRoles(@PathVariable Long id, @RequestParam List<Long> roles) {
    Usuario usuario = usuarioRepository.findById(id).orElseThrow();
    List<Rol> nuevosRoles = rolRepository.findAllById(roles);

    // Limpiar los roles previos
    usuario.getUsuarioRoles().clear();

    // Asignar nuevos
    for (Rol rol : nuevosRoles) {
        UsuarioRol ur = new UsuarioRol();
        ur.setUsuario(usuario);
        ur.setRol(rol);
        usuario.getUsuarioRoles().add(ur);
    }

    usuarioRepository.save(usuario); // ✅ ahora se guarda en usuario_roles
    return "redirect:/admin/usuarios";
}

}
