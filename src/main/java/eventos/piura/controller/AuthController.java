package eventos.piura.controller;

import eventos.piura.dto.RecuperarContrasenaRequest;
import eventos.piura.dto.RegistroUsuarioRequest;
import eventos.piura.dto.RestablecerContrasenaRequest;
import eventos.piura.model.Usuario;
import eventos.piura.services.RecuperacionContrasenaService;
import eventos.piura.services.UsuarioService;
import eventos.piura.services.VerificacionCorreoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private VerificacionCorreoService verificacionCorreoService;

    @Autowired
    private RecuperacionContrasenaService recuperacionContrasenaService;

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        if (!model.containsAttribute("registroRequest")) {
            model.addAttribute("registroRequest", new RegistroUsuarioRequest());
        }
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroRequest") RegistroUsuarioRequest request,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/registro";
        }

        try {
            Usuario usuario = usuarioService.registrar(request);
            redirectAttributes.addFlashAttribute("success",
                    "Hemos enviado un correo de verificacion. Revisa tu bandeja para activar la cuenta.");
            redirectAttributes.addFlashAttribute("correoPendiente", usuario.getCorreo());
            return "redirect:/auth/verificacion-pendiente";
        } catch (IllegalArgumentException e) {
            String errorMessage = e.getMessage();
            String normalized = errorMessage == null ? "" : errorMessage.toLowerCase();
            if (normalized.contains("usuario ya existe")) {
                result.rejectValue("username", "error.username", errorMessage);
            } else if (normalized.contains("correo") && normalized.contains("registr")) {
                result.rejectValue("correo", "error.correo", errorMessage);
            } else if (normalized.contains("dni") && normalized.contains("registr")) {
                result.rejectValue("dni", "error.dni", errorMessage);
            } else if (normalized.contains("contras")) {
                result.rejectValue("confirmarContrasena", "error.confirmarContrasena", errorMessage);
            } else {
                result.rejectValue("correo", "error.registro", errorMessage);
            }
            return "auth/registro";
        } catch (IllegalStateException e) {
            result.reject("error.registro", e.getMessage());
            return "auth/registro";
        }
    }

    @GetMapping("/verificacion-pendiente")
    public String verificacionPendiente(@ModelAttribute("correoPendiente") String correoPendiente, Model model) {
        if (correoPendiente != null && !correoPendiente.isBlank()) {
            model.addAttribute("correoPendiente", correoPendiente);
        }
        return "auth/verificacion-pendiente";
    }

    @PostMapping("/verificacion/reenviar")
    public String reenviarVerificacion(@RequestParam("correo") String correo,
                                       RedirectAttributes redirectAttributes) {
        usuarioService.obtenerPorCorreo(correo)
                .filter(usuario -> !usuario.isCorreoVerificado())
                .ifPresentOrElse(usuario -> {
                    verificacionCorreoService.crearSolicitudVerificacion(usuario);
                    redirectAttributes.addFlashAttribute("success",
                            "Hemos reenviado el correo de verificacion a " + usuario.getCorreo());
                    redirectAttributes.addFlashAttribute("correoPendiente", usuario.getCorreo());
                }, () -> redirectAttributes.addFlashAttribute("error",
                        "No encontramos una cuenta pendiente de verificacion para el correo ingresado."));
        return "redirect:/auth/verificacion-pendiente";
    }

    @GetMapping("/confirmar")
    public String confirmarCorreo(@RequestParam("codigo") String codigo, RedirectAttributes redirectAttributes) {
        boolean confirmado = verificacionCorreoService.confirmarCodigo(codigo);
        if (confirmado) {
            redirectAttributes.addFlashAttribute("success",
                    "Correo verificado correctamente. Ya puedes iniciar sesion.");
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "El codigo de verificacion es invalido o ha expirado. Solicita uno nuevo.");
        }
        return "redirect:/auth/login";
    }

    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        if (error != null && !model.containsAttribute("error")) {
            model.addAttribute("error", "Usuario o contrasena incorrectos");
        }
        if (logout != null) {
            model.addAttribute("success", "Has cerrado sesion exitosamente");
        }
        return "auth/login";
    }

    @GetMapping("/recuperar")
    public String mostrarRecuperacion(Model model) {
        if (!model.containsAttribute("recuperarRequest")) {
            model.addAttribute("recuperarRequest", new RecuperarContrasenaRequest());
        }
        return "auth/recuperar";
    }

    @PostMapping("/recuperar")
    public String solicitarRecuperacion(@Valid @ModelAttribute("recuperarRequest") RecuperarContrasenaRequest request,
                                        BindingResult result,
                                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/recuperar";
        }
        if (request.getCorreo() != null) {
            request.setCorreo(request.getCorreo().trim().toLowerCase());
        }
        recuperacionContrasenaService.solicitarRecuperacion(request.getCorreo());
        redirectAttributes.addFlashAttribute("success",
                "Si el correo esta registrado enviaremos instrucciones para restablecer la contrasena.");
        return "redirect:/auth/recuperar";
    }

    @GetMapping("/restablecer")
    public String mostrarRestablecer(@RequestParam("token") String token,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (!recuperacionContrasenaService.tokenValido(token)) {
            redirectAttributes.addFlashAttribute("error",
                    "El enlace de restablecimiento no es valido o ha expirado. Solicita uno nuevo.");
            return "redirect:/auth/recuperar";
        }
        if (!model.containsAttribute("restablecerRequest")) {
            RestablecerContrasenaRequest form = new RestablecerContrasenaRequest();
            form.setToken(token);
            model.addAttribute("restablecerRequest", form);
        }
        return "auth/restablecer";
    }

    @PostMapping("/restablecer")
    public String procesarRestablecer(@Valid @ModelAttribute("restablecerRequest") RestablecerContrasenaRequest request,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes) {
        if (request.getToken() != null) {
            request.setToken(request.getToken().trim());
        }
        if (request.getContrasena() != null) {
            request.setContrasena(request.getContrasena().trim());
        }
        if (request.getConfirmarContrasena() != null) {
            request.setConfirmarContrasena(request.getConfirmarContrasena().trim());
        }
        if (!request.getContrasena().equals(request.getConfirmarContrasena())) {
            result.rejectValue("confirmarContrasena", "error.confirmarContrasena", "Las contrasenas no coinciden");
        }
        if (result.hasErrors()) {
            return "auth/restablecer";
        }
        boolean restablecido = recuperacionContrasenaService.restablecerContrasena(
                request.getToken(), request.getContrasena());
        if (!restablecido) {
            result.reject("error.restablecer", "El token no es valido o ya fue utilizado. Solicita uno nuevo.");
            return "auth/restablecer";
        }
        redirectAttributes.addFlashAttribute("success",
                "Tu contrasena se actualizo correctamente. Inicia sesion con la nueva credencial.");
        return "redirect:/auth/login";
    }
}
