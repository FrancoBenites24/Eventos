package eventos.piura.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import eventos.piura.dto.RegistroUsuarioRequest;
import eventos.piura.model.Usuario;
import eventos.piura.service.UsuarioService;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private AuthenticationManager authenticationManager;

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("registroRequest", new RegistroUsuarioRequest());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroRequest") RegistroUsuarioRequest request,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "auth/registro";

        try {
            Usuario usuario = usuarioService.registrar(request);
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getContrasena())
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            redirectAttributes.addFlashAttribute("success", "Cuenta creada exitosamente. ¡Bienvenido!");
            return "redirect:/admin";
        } catch (IllegalArgumentException e) {
            // usa el nombre real del campo en tu DTO: "email" o "correo"
            result.rejectValue("email", "error.registro", e.getMessage());
            return "auth/registro";
        }
    }

    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        if (error != null) model.addAttribute("error", "Usuario o contraseña incorrectos");
        if (logout != null) model.addAttribute("success", "Has cerrado sesión exitosamente");
        return "auth/login";
    }
}
