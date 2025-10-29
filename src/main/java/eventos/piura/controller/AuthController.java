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
import eventos.piura.services.UsuarioService;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuthenticationManager authenticationManager;

    // GET - Mostrar formulario de registro
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("registroRequest", new RegistroUsuarioRequest());
        return "auth/registro";
    }

    // POST - Procesar registro
    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroRequest") RegistroUsuarioRequest request,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/registro";
        }

        try {
            Usuario usuario = usuarioService.registrar(request);
            // Autenticar automáticamente al usuario después del registro
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getContrasena())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            redirectAttributes.addFlashAttribute("success", "Cuenta creada exitosamente. Bienvenido!");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            // Determinar el campo específico del error para mostrar en tiempo real
            String errorMessage = e.getMessage();
            if (errorMessage.contains("nombre de usuario ya existe")) {
                result.rejectValue("username", "error.username", errorMessage);
            } else if (errorMessage.contains("correo electrónico ya está registrado")) {
                result.rejectValue("correo", "error.correo", errorMessage);
            } else if (errorMessage.contains("DNI ya está registrado")) {
                result.rejectValue("dni", "error.dni", errorMessage);
            } else if (errorMessage.contains("contraseñas no coinciden")) {
                result.rejectValue("confirmarContrasena", "error.confirmarContrasena", errorMessage);
            } else {
                result.rejectValue("correo", "error.registro", errorMessage);
            }
            return "auth/registro";
        }
    }

    // GET - Mostrar formulario de login
    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        if (logout != null) {
            model.addAttribute("success", "Has cerrado sesión exitosamente");
        }
        return "auth/login";
    }
}
