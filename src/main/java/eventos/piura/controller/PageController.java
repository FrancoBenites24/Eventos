package eventos.piura.controller;

import eventos.piura.dto.PerfilPageData;
import eventos.piura.services.PerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final PerfilService perfilService;

    @GetMapping("/perfil")
    public String perfil(Model model, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        PerfilPageData perfil = perfilService.cargarPerfil(authentication.getName());

        model.addAttribute("usuario", perfil.usuario());
        model.addAttribute("nivelActual", perfil.gamificacion().nivel());
        model.addAttribute("puntosActuales", perfil.gamificacion().puntos());
        model.addAttribute("progresoPct", perfil.gamificacion().progresoPct());
        model.addAttribute("puntosRestantes", perfil.gamificacion().puntosRestantes());
        model.addAttribute("stats", perfil.stats());
        model.addAttribute("billeteraSaldo", perfil.billeteraSaldo());
        model.addAttribute("eventos", perfil.eventos());
        model.addAttribute("resenas", Collections.emptyList());
        model.addAttribute("txs", perfil.transacciones());
        model.addAttribute("ordenes", perfil.ordenes());

        return "perfil/perfil";
    }
}


sadfghjklñljlhgfdfsadfgh
