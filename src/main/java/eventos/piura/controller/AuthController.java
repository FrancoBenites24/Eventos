package eventos.piura.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import eventos.piura.model.Usuario;
import eventos.piura.services.UsuarioService;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    // GET - Mostrar formulario de login
    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    // POST - Procesar login
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model) {
        return usuarioService.login(username, password)
                .map(u -> {
                    model.addAttribute("success", "Bienvenido " + u.getNombre());
                    return "redirect:/";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Usuario o contraseña incorrectos");
                    return "login";
                });
    }

    // GET - Mostrar formulario de registro
    @GetMapping("/register")
    public String mostrarRegistro() {
        return "registro";
    }

    // POST - Procesar registro
    @PostMapping("/register")
    public String registrar(@RequestParam String nombre,
                            @RequestParam String apellido,
                            @RequestParam String username,
                            @RequestParam String email,
                            @RequestParam String telefono,
                            @RequestParam String password,
                            @RequestParam String confirmPassword,
                            Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            return "register";
        }

        if (usuarioService.existePorEmail(email)) {
            model.addAttribute("error", "El email ya está registrado");
            return "register";
        }

        if (usuarioService.existePorUsername(username)) {
            model.addAttribute("error", "El nombre de usuario ya está en uso");
            return "register";
        }

        if (!telefono.matches("\\d{9}")) {
            model.addAttribute("error", "El teléfono debe tener 9 dígitos");
            return "register";
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setTelefono(telefono);
        usuario.setPassword(password);

        usuarioService.registrar(usuario);

        model.addAttribute("success", "Registro exitoso, ya puedes iniciar sesión");
        return "login";
    }
}
