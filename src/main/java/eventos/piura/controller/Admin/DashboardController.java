package eventos.piura.controller.Admin;

import eventos.piura.repository.UsuarioRepository;
import eventos.piura.repository.RolRepository;
import eventos.piura.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    @GetMapping
    public String dashboard(Model model) {
        // Totales
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalRoles", rolRepository.count());
        model.addAttribute("totalPermisos", permisoRepository.count());

        // Últimos 5 usuarios
        model.addAttribute("ultimosUsuarios", usuarioRepository.findTop5ByOrderByCreadoEnDesc());

        model.addAttribute("content", "admin/dashboard");
        model.addAttribute("title", "Dashboard");

        return "admin/layout";
    }
}
