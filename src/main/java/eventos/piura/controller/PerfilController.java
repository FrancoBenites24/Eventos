    package eventos.piura.controller;

    import eventos.piura.dto.PerfilDTO;
    import eventos.piura.model.Usuario;
    import eventos.piura.repository.UsuarioRepository;
    import eventos.piura.services.PerfilService;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
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

    import java.net.URLEncoder;
    import java.nio.charset.StandardCharsets;
    import java.util.ArrayList;

    @Controller
    public class PerfilController {

        private static final Logger logger = LoggerFactory.getLogger(PerfilController.class);

        @Autowired
        private PerfilService perfilService;

        @Autowired
        private UsuarioRepository usuarioRepository;

        @GetMapping("/perfil")
        public String mostrarPerfil(Model model) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            if (username == null || username.equals("anonymousUser")) {
                return "redirect:/login";
            }

            PerfilDTO perfil;
            try {
                perfil = perfilService.obtenerPerfil(username);
            } catch (Exception e) {
                logger.error("Error al obtener perfil: {}", e.getMessage(), e);
                perfil = new PerfilDTO();
                perfil.setPuntos(0);
                perfil.setNivel(0);
                perfil.setPuntosParaSiguienteNivel(40);
                perfil.setSaldo(0.0);
                perfil.setProximosEventos(new ArrayList<>());
                perfil.setMovimientosRecientes(new ArrayList<>());
            }

            model.addAttribute("perfil", perfil);
            return "profile";
        }

        @GetMapping("/perfil/editar")
        public String mostrarFormularioEditarPerfil(Model model) {
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth.getName();

                Usuario usuario = usuarioRepository.findByUsernameWithRoles(username)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

                model.addAttribute("usuario", usuario);
                return "editarperfil";

            } catch (Exception e) {
                logger.error("Error al cargar formulario de edición: {}", e.getMessage(), e);
                return "redirect:/perfil?error=" + URLEncoder.encode("Error al cargar el formulario", StandardCharsets.UTF_8);
            }
        }

        @PostMapping("/perfil/editar")
        public String editarPerfil(
                @RequestParam("nombre") String nombre,
                @RequestParam("apellido") String apellido,
                @RequestParam("username") String username,
                @RequestParam("email") String email,
                @RequestParam(value = "telefono", required = false) String telefono,
                @RequestParam(value = "fotoPerfil", required = false) MultipartFile fotoPerfil,
                RedirectAttributes redirectAttributes) {

            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String currentUsername = auth.getName();

                Usuario usuario = usuarioRepository.findByUsernameWithRoles(currentUsername)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + currentUsername));

                usuario.setNombre(nombre);
                usuario.setApellido(apellido);
                usuario.setUsername(username);
                usuario.setEmail(email);

                if (telefono != null && !telefono.isEmpty()) usuario.setTelefono(telefono);

                if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                    String fileName = System.currentTimeMillis() + "_" + fotoPerfil.getOriginalFilename();
                    usuario.setFotoPerfil(fileName);
                    // Aquí puedes guardar el archivo en disco si deseas
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

