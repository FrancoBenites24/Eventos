package eventos.piura.services;

import eventos.piura.dto.EventoDTO;
import eventos.piura.dto.MovimientoBilleteraDTO;
import eventos.piura.dto.PerfilDTO;
import eventos.piura.model.*;
import eventos.piura.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerfilService {

    private static final Logger logger = LoggerFactory.getLogger(PerfilService.class);
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private BilleteraService billeteraService;
    
    @Autowired
    private EventoService eventoService;

    @Transactional(readOnly = true)
    public PerfilDTO obtenerPerfil(String username) {
        logger.info("Iniciando obtención de perfil para el usuario: {}", username);
        
        try {
            // 1. Obtener usuario con roles
            logger.info("Buscando usuario en la base de datos...");
            Usuario usuario = usuarioRepository.findByUsernameWithRoles(username)
                    .orElseThrow(() -> {
                        logger.error("Usuario no encontrado en la base de datos: {}", username);
                        return new RuntimeException("Usuario no encontrado: " + username);
                    });
            
            logger.info("Usuario encontrado: {}", usuario.getUsername());
            logger.info("ID del usuario: {}", usuario.getId());
            logger.info("Nombre: {}", usuario.getNombre());
            logger.info("Apellido: {}", usuario.getApellido());
            logger.info("Email: {}", usuario.getEmail());
            logger.info("Roles: {}", usuario.getUsuarioRoles().stream()
                    .map(ur -> ur.getRol().getNombre())
                    .collect(Collectors.joining(", ")));
            
            // 2. Crear el DTO básico con datos del usuario
            PerfilDTO perfilDTO = new PerfilDTO();
            perfilDTO.setId(usuario.getId());
            perfilDTO.setUsername(usuario.getUsername());
            perfilDTO.setNombre(usuario.getNombre() != null ? usuario.getNombre() : "");
            perfilDTO.setApellido(usuario.getApellido() != null ? usuario.getApellido() : "");
            perfilDTO.setEmail(usuario.getEmail());
            perfilDTO.setTelefono(usuario.getTelefono());
            perfilDTO.setFotoPerfil(usuario.getFotoPerfil());
            perfilDTO.setCreadoEn(usuario.getCreadoEn());
            
            // Construir nombre completo
            String nombreCompleto = "";
            if (usuario.getNombre() != null && !usuario.getNombre().isEmpty()) {
                nombreCompleto += usuario.getNombre();
            }
            if (usuario.getApellido() != null && !usuario.getApellido().isEmpty()) {
                if (!nombreCompleto.isEmpty()) {
                    nombreCompleto += " ";
                }
                nombreCompleto += usuario.getApellido();
            }
            perfilDTO.setNombreCompleto(nombreCompleto.isEmpty() ? usuario.getUsername() : nombreCompleto);
            
            // Roles del usuario
            perfilDTO.setRoles(usuario.getUsuarioRoles().stream()
                    .map(ur -> ur.getRol().getNombre())
                    .collect(Collectors.toList()));
            
            // 3. Datos adicionales (no están en la base de datos)
            perfilDTO.setBiografia("Amante de la tecnología y los eventos. Siempre buscando nuevas experiencias.");
            perfilDTO.setPuntos(60);
            perfilDTO.setNivel(1);
            perfilDTO.setPuntosParaSiguienteNivel(40);
            perfilDTO.setVerificado(true);
            perfilDTO.setMiembro(true);
            
            // 4. Obtener billetera
            logger.info("Buscando billetera del usuario...");
            try {
                Billetera billetera = billeteraService.obtenerPorUsuarioId(usuario.getId())
                        .orElseGet(() -> {
                            logger.info("No se encontró billetera, creando una nueva...");
                            return billeteraService.crearBilleteraParaUsuario(usuario);
                        });
                
                perfilDTO.setSaldo(billetera.getSaldo() != null ? billetera.getSaldo() : 0.0);
                logger.info("Saldo de la billetera: {}", perfilDTO.getSaldo());
                
                // 5. Obtener movimientos recientes
                logger.info("Buscando movimientos recientes...");
                List<MovimientoBilletera> movimientos = billeteraService.obtenerMovimientosRecientes(usuario.getId());
                logger.info("Se encontraron {} movimientos recientes", movimientos.size());
                
                perfilDTO.setMovimientosRecientes(movimientos.stream()
                        .map(this::convertirMovimientoADTO)
                        .collect(Collectors.toList()));
            } catch (Exception e) {
                logger.error("Error al obtener billetera o movimientos: {}", e.getMessage(), e);
                perfilDTO.setSaldo(0.0);
                perfilDTO.setMovimientosRecientes(Collections.emptyList());
            }
            
            // 6. Obtener próximos eventos
            logger.info("Buscando próximos eventos...");
            try {
                List<Evento> eventos = eventoService.obtenerProximosEventos(usuario.getId());
                logger.info("Se encontraron {} próximos eventos", eventos.size());
                
                perfilDTO.setProximosEventos(eventos.stream()
                        .map(this::convertirEventoADTO)
                        .collect(Collectors.toList()));
            } catch (Exception e) {
                logger.error("Error al obtener próximos eventos: {}", e.getMessage(), e);
                perfilDTO.setProximosEventos(Collections.emptyList());
            }
            
            logger.info("Perfil creado exitosamente para el usuario: {}", username);
            return perfilDTO;
            
        } catch (Exception e) {
            logger.error("Error al obtener el perfil para el usuario {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }
    
    private MovimientoBilleteraDTO convertirMovimientoADTO(MovimientoBilletera movimiento) {
        try {
            MovimientoBilleteraDTO dto = new MovimientoBilleteraDTO();
            dto.setId(movimiento.getId());
            dto.setMonto(movimiento.getMonto());
            dto.setTipo(movimiento.getTipo());
            dto.setDescripcion(movimiento.getDescripcion());
            dto.setCreadoEn(movimiento.getCreadoEn());
            return dto;
        } catch (Exception e) {
            logger.error("Error al convertir movimiento a DTO: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private EventoDTO convertirEventoADTO(Evento evento) {
        try {
            EventoDTO dto = new EventoDTO();
            dto.setId(evento.getId());
            dto.setTitulo(evento.getTitulo());
            dto.setDescripcion(evento.getDescripcion());
            dto.setFechaInicio(evento.getFechaInicio());
            dto.setFechaFin(evento.getFechaFin());
            dto.setDireccion(evento.getDireccion());
            dto.setDistrito(evento.getDistrito());
            dto.setCiudad(evento.getCiudad());
            dto.setEsGratuito(evento.getEsGratuito());
            dto.setBanner(evento.getBanner());
            return dto;
        } catch (Exception e) {
            logger.error("Error al convertir evento a DTO: {}", e.getMessage(), e);
            return null;
        }
    }
}