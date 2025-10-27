package eventos.piura.controller;

import eventos.piura.dto.UsuarioResumenView;
import eventos.piura.mapper.UsuarioViewMapper;
import eventos.piura.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioViewMapper usuarioViewMapper;

    @GetMapping("/perfil")
    public String perfil(Model model, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        UsuarioResumenView usuario = usuarioRepository.findByUsernameIgnoreCase(authentication.getName())
                .map(usuarioViewMapper::mapear)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("usuario", usuario);
        model.addAttribute("nivelActual", 0);
        model.addAttribute("puntosActuales", 0);
        model.addAttribute("progresoPct", 0);
        model.addAttribute("puntosRestantes", 0);
        model.addAttribute("stats", Map.of(
                "eventosAsistidos", 0,
                "resenas", 0,
                "ordenes", 0,
                "entradasActivas", 0));
        model.addAttribute("billeteraSaldo", 0.0);
        model.addAttribute("eventos", Collections.emptyList());
        model.addAttribute("resenas", Collections.emptyList());
        model.addAttribute("txs", Collections.emptyList());
        model.addAttribute("ordenes", Collections.emptyList());

        return "perfil/perfil";
    }
}
