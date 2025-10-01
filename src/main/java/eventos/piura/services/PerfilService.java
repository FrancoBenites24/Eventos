package eventos.piura.services;

import eventos.piura.dto.EventoDTO;
import eventos.piura.dto.MovimientoBilleteraDTO;
import eventos.piura.dto.PerfilDTO;
import eventos.piura.model.*;
import eventos.piura.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class PerfilService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BilleteraService billeteraService;

    @Autowired
    private EventoService eventoService;

    @Transactional(readOnly = true)
    public PerfilDTO obtenerPerfil(String username) {
        Usuario usuario = usuarioRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        PerfilDTO perfil = new PerfilDTO();

        // Datos básicos
        perfil.setId(usuario.getId());
        perfil.setUsername(usuario.getUsername() != null ? usuario.getUsername() : "Usuario");
        perfil.setNombre(usuario.getNombre() != null ? usuario.getNombre() : "");
        perfil.setApellido(usuario.getApellido() != null ? usuario.getApellido() : "");
        perfil.setEmail(usuario.getEmail() != null ? usuario.getEmail() : "correo@ejemplo.com");
        perfil.setTelefono(usuario.getTelefono() != null ? usuario.getTelefono() : "");
        perfil.setFotoPerfil(usuario.getFotoPerfil() != null ? usuario.getFotoPerfil() : "https://via.placeholder.com/80");
        perfil.setCreadoEn(usuario.getCreadoEn());

        // Nombre completo
        String nombreCompleto = (perfil.getNombre() + " " + perfil.getApellido()).trim();
        perfil.setNombreCompleto(!nombreCompleto.isEmpty() ? nombreCompleto : perfil.getUsername());

        // Roles
        perfil.setRoles(usuario.getUsuarioRoles() != null ?
                usuario.getUsuarioRoles().stream().map(ur -> ur.getRol().getNombre()).collect(Collectors.toList()) :
                new ArrayList<>());

        // Datos adicionales
        perfil.setBiografia("Sin biografía");
        perfil.setPuntos(0);
        perfil.setNivel(0);
        perfil.setPuntosParaSiguienteNivel(40);
        perfil.setVerificado(true);
        perfil.setMiembro(true);

        // Billetera y movimientos
        try {
            Billetera billetera = billeteraService.obtenerPorUsuarioId(usuario.getId())
                    .orElseGet(() -> billeteraService.crearBilleteraParaUsuario(usuario));
            perfil.setSaldo(billetera.getSaldo() != null ? billetera.getSaldo() : 0.0);

            List<MovimientoBilletera> movimientos = billeteraService.obtenerMovimientosRecientes(usuario.getId());
            perfil.setMovimientosRecientes(movimientos != null ?
                    movimientos.stream().map(this::convertirMovimientoADTO).collect(Collectors.toList()) :
                    new ArrayList<>());
        } catch (Exception e) {
            perfil.setSaldo(0.0);
            perfil.setMovimientosRecientes(new ArrayList<>());
        }

        // Próximos eventos
        try {
            List<Evento> eventos = eventoService.obtenerProximosEventos(usuario.getId());
            perfil.setProximosEventos(eventos != null ?
                    eventos.stream().map(this::convertirEventoADTO).collect(Collectors.toList()) :
                    new ArrayList<>());
        } catch (Exception e) {
            perfil.setProximosEventos(new ArrayList<>());
        }

        // **Garantizar valores no nulos para la columna derecha**
        if (perfil.getPuntos() == null) perfil.setPuntos(0);
        if (perfil.getNivel() == null) perfil.setNivel(0);
        if (perfil.getPuntosParaSiguienteNivel() == null) perfil.setPuntosParaSiguienteNivel(40);
        if (perfil.getProximosEventos() == null) perfil.setProximosEventos(new ArrayList<>());

        return perfil;
    }

    private MovimientoBilleteraDTO convertirMovimientoADTO(MovimientoBilletera m) {
        MovimientoBilleteraDTO dto = new MovimientoBilleteraDTO();
        dto.setId(m.getId());
        dto.setMonto(m.getMonto() != null ? m.getMonto() : 0.0);
        dto.setTipo(m.getTipo() != null ? m.getTipo() : "desconocido");
        dto.setDescripcion(m.getDescripcion() != null ? m.getDescripcion() : dto.getTipo());
        dto.setCreadoEn(m.getCreadoEn() != null ? m.getCreadoEn() : java.time.LocalDateTime.now());
        return dto;
    }

    private EventoDTO convertirEventoADTO(Evento e) {
        EventoDTO dto = new EventoDTO();
        dto.setId(e.getId());
        dto.setTitulo(e.getTitulo() != null ? e.getTitulo() : "Evento");
        dto.setDescripcion(e.getDescripcion() != null ? e.getDescripcion() : "Sin descripción");
        dto.setFechaInicio(e.getFechaInicio() != null ? e.getFechaInicio() : java.time.LocalDateTime.now());
        dto.setFechaFin(e.getFechaFin() != null ? e.getFechaFin() : dto.getFechaInicio());
        dto.setDireccion(e.getDireccion() != null ? e.getDireccion() : "-");
        dto.setDistrito(e.getDistrito() != null ? e.getDistrito() : "-");
        dto.setCiudad(e.getCiudad() != null ? e.getCiudad() : "-");
        dto.setEsGratuito(e.getEsGratuito() != null ? e.getEsGratuito() : false);
        dto.setBanner(e.getBanner() != null ? e.getBanner() : "https://via.placeholder.com/150");
        return dto;
    }
}

