package eventos.piura.controller;

import eventos.piura.dto.EventoDTO;
import eventos.piura.dto.MovimientoBilleteraDTO;
import eventos.piura.dto.PerfilDTO;
import eventos.piura.services.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class PerfilController {

    private static final Logger logger = LoggerFactory.getLogger(PerfilController.class);
    
    @Autowired
    private PerfilService perfilService;


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
        logger.info("Perfil cargado exitosamente para el usuario: {}", username);
        
        return "profile";
    } catch (Exception e) {
        logger.error("Error al cargar el perfil: {}", e.getMessage(), e);
        model.addAttribute("error", "Error al cargar el perfil: " + e.getMessage());
        return "profile";
    }
}
    @GetMapping("/debug-perfil-dto")
@ResponseBody
public String debugPerfilDTO() {
    try {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        logger.info("Depurando perfil DTO para el usuario: {}", username);
        
        PerfilDTO perfil = perfilService.obtenerPerfil(username);
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== PERFIL DTO ===\n");
        sb.append("ID: ").append(perfil.getId()).append("\n");
        sb.append("Username: ").append(perfil.getUsername()).append("\n");
        sb.append("Nombre: ").append(perfil.getNombre()).append("\n");
        sb.append("Apellido: ").append(perfil.getApellido()).append("\n");
        sb.append("Nombre Completo: ").append(perfil.getNombreCompleto()).append("\n");
        sb.append("Email: ").append(perfil.getEmail()).append("\n");
        sb.append("Saldo: ").append(perfil.getSaldo()).append("\n");
        sb.append("Puntos: ").append(perfil.getPuntos()).append("\n");
        sb.append("Nivel: ").append(perfil.getNivel()).append("\n");
        sb.append("Roles: ").append(String.join(", ", perfil.getRoles())).append("\n");
        sb.append("=== MOVIMIENTOS ===\n");
        
        for (MovimientoBilleteraDTO movimiento : perfil.getMovimientosRecientes()) {
            sb.append("- ").append(movimiento.getTipo()).append(": ").append(movimiento.getMonto()).append("\n");
        }
        
        sb.append("=== EVENTOS ===\n");
        for (EventoDTO evento : perfil.getProximosEventos()) {
            sb.append("- ").append(evento.getTitulo()).append(": ").append(evento.getFechaInicio()).append("\n");
        }
        
        return sb.toString();
    } catch (Exception e) {
        logger.error("Error en debug-perfil-dto: {}", e.getMessage(), e);
        return "Error: " + e.getMessage();
    }
}
    @GetMapping("/crear-perfil-prueba")
public String crearPerfilPrueba(Model model) {
    try {
        logger.info("Creando perfil de prueba...");
        
        // Crear un perfil de prueba
        PerfilDTO perfil = new PerfilDTO();
        perfil.setId(1L);
        perfil.setUsername("usuario_prueba");
        perfil.setNombre("Usuario");
        perfil.setApellido("Prueba");
        perfil.setEmail("prueba@ejemplo.com");
        perfil.setNombreCompleto("Usuario Prueba");
        perfil.setBiografia("Este es un perfil de prueba");
        perfil.setPuntos(60);
        perfil.setNivel(1);
        perfil.setPuntosParaSiguienteNivel(40);
        perfil.setVerificado(true);
        perfil.setMiembro(true);
        perfil.setSaldo(20100.0);
        
        // Crear algunos movimientos de prueba
        List<MovimientoBilleteraDTO> movimientos = new ArrayList<>();
        
        MovimientoBilleteraDTO movimiento1 = new MovimientoBilleteraDTO();
        movimiento1.setId(1L);
        movimiento1.setMonto(500.0);
        movimiento1.setTipo("recarga");
        movimiento1.setDescripcion("Recarga de saldo");
        movimiento1.setCreadoEn(LocalDateTime.now().minusDays(1));
        movimientos.add(movimiento1);
        
        MovimientoBilleteraDTO movimiento2 = new MovimientoBilleteraDTO();
        movimiento2.setId(2L);
        movimiento2.setMonto(150.0);
        movimiento2.setTipo("compra");
        movimiento2.setDescripcion("Compra de boletos");
        movimiento2.setCreadoEn(LocalDateTime.now().minusDays(3));
        movimientos.add(movimiento2);
        
        perfil.setMovimientosRecientes(movimientos);
        
        // Crear algunos eventos de prueba
        List<EventoDTO> eventos = new ArrayList<>();
        
        EventoDTO evento1 = new EventoDTO();
        evento1.setId(1L);
        evento1.setTitulo("JS Summit Lima");
        evento1.setDescripcion("El evento más importante de JavaScript en Lima");
        evento1.setFechaInicio(LocalDateTime.now().plusDays(10));
        evento1.setFechaFin(LocalDateTime.now().plusDays(10).plusHours(8));
        evento1.setDireccion("Av. Javier Prado Este 1234");
        evento1.setDistrito("San Borja");
        evento1.setCiudad("Lima");
        evento1.setEsGratuito(false);
        evento1.setBanner("https://via.placeholder.com/300x200");
        eventos.add(evento1);
        
        perfil.setProximosEventos(eventos);
        
        // Agregar datos al modelo
        model.addAttribute("perfil", perfil);
        
        logger.info("Perfil de prueba creado exitosamente");
        return "profile";
    } catch (Exception e) {
        logger.error("Error al crear perfil de prueba: {}", e.getMessage(), e);
        model.addAttribute("error", "Error al crear perfil de prueba: " + e.getMessage());
        return "profile";
    }
}
}