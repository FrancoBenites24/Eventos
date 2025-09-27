package eventos.piura.controller;

import eventos.piura.model.Usuario;
import eventos.piura.dto.PerfilDTO;
import eventos.piura.services.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class PerfilController {

    private static final Logger logger = LoggerFactory.getLogger(PerfilController.class);
    
    @Autowired
    private PerfilService perfilService;

    @Autowired
    private eventos.piura.repository.UsuarioRepository usuarioRepository;
@GetMapping("/perfil")
public String mostrarPerfil(Model model) {
    try {
        // Obtener usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        logger.info("Usuario autenticado: {}", username);
        
        // Verificar si el usuario está autenticado
        if (username == null || username.equals("anonymousUser")) {
            logger.warn("Usuario no autenticado intentando acceder al perfil");
            return "redirect:/login";
        }
        
        // Obtener datos del perfil
        PerfilDTO perfil = perfilService.obtenerPerfil(username);
        
        if (perfil == null) {
            logger.error("El perfil es nulo para el usuario: {}", username);
            model.addAttribute("error", "No se encontró información del perfil");
            return "profile";
        }
        
        // Agregar datos al modelo
        model.addAttribute("perfil", perfil);
        
        // Agregar datos individuales para mayor seguridad
        model.addAttribute("nombreCompleto", perfil.getNombreCompleto() != null ? perfil.getNombreCompleto() : "");
        model.addAttribute("email", perfil.getEmail() != null ? perfil.getEmail() : "");
        model.addAttribute("biografia", perfil.getBiografia() != null ? perfil.getBiografia() : "");
        model.addAttribute("puntos", perfil.getPuntos() != null ? perfil.getPuntos() : 0);
        model.addAttribute("nivel", perfil.getNivel() != null ? perfil.getNivel() : 0);
        model.addAttribute("puntosParaSiguienteNivel", perfil.getPuntosParaSiguienteNivel() != null ? perfil.getPuntosParaSiguienteNivel() : 40);
        model.addAttribute("saldo", perfil.getSaldo() != null ? perfil.getSaldo() : 0.0);
        model.addAttribute("movimientosRecientes", perfil.getMovimientosRecientes() != null ? perfil.getMovimientosRecientes() : new ArrayList<>());
        model.addAttribute("proximosEventos", perfil.getProximosEventos() != null ? perfil.getProximosEventos() : new ArrayList<>());
        
        logger.info("Perfil cargado exitosamente para el usuario: {}", username);
        
        return "profile";
    } catch (Exception e) {
        logger.error("Error al cargar el perfil: {}", e.getMessage(), e);
        model.addAttribute("error", "Error al cargar el perfil: " + e.getMessage());
        return "profile";
    }
}
   @GetMapping("/perfil/editar")
public String mostrarFormularioEditarPerfil(Model model) {
    try {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Usuario usuario = usuarioRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("biografia", "Amante de la tecnología y los eventos. Siempre buscando nuevas experiencias.");
        
        return "editarperfil";
    } catch (Exception e) {
        logger.error("Error al cargar formulario de edición: {}", e.getMessage(), e);
        return "redirect:/perfil?error=" + URLEncoder.encode("Error al cargar el formulario de edición", StandardCharsets.UTF_8);
    }
}

@PostMapping("/perfil/editar")
public String editarPerfil(
        @RequestParam("nombre") String nombre,
        @RequestParam("apellido") String apellido,
        @RequestParam("username") String username,
        @RequestParam("email") String email,
        @RequestParam(value = "telefono", required = false) String telefono,
        @RequestParam(value = "biografia", required = false) String biografia,
        @RequestParam(value = "fotoPerfil", required = false) MultipartFile fotoPerfil,
        RedirectAttributes redirectAttributes) {
    
    try {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        
        Usuario usuario = usuarioRepository.findByUsernameWithRoles(currentUsername)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + currentUsername));
        
        // Actualizar datos del usuario
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setUsername(username);
        usuario.setEmail(email);
        
        if (telefono != null && !telefono.isEmpty()) {
            usuario.setTelefono(telefono);
        }
        
        // Guardar la foto de perfil si se proporcionó
        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            // Aquí deberías implementar la lógica para guardar la imagen
            // Por ahora, solo guardamos el nombre del archivo
            String fileName = System.currentTimeMillis() + "_" + fotoPerfil.getOriginalFilename();
            usuario.setFotoPerfil(fileName);
            
            // Aquí guardarías el archivo en el sistema de archivos o en la nube
            // Path path = Paths.get("uploads/" + fileName);
            // Files.copy(fotoPerfil.getInputStream(), path);
        }
        
        usuarioRepository.save(usuario);
        
        redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
        return "redirect:/perfil";
        
    } catch (Exception e) {
        logger.error("Error al editar perfil: {}", e.getMessage(), e);
        redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
        return "redirect:/perfil/editar";
    }
}

}