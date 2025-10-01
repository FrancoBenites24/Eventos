package eventos.piura.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PerfilDTO {
    // Datos del usuario
    private Long id;
    private String username;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String fotoPerfil;
    private LocalDateTime creadoEn;
    private List<String> roles;
    
    // Datos adicionales para el perfil
    private String nombreCompleto;
    private String biografia;
    private Integer puntos;
    private Integer nivel;
    private Integer puntosParaSiguienteNivel;
    private Boolean verificado;
    private Boolean miembro;
    
    // Datos de la billetera
    private Double saldo;
    
    // Movimientos de billetera
    private List<MovimientoBilleteraDTO> movimientosRecientes;
    
    // Próximos eventos
    private List<EventoDTO> proximosEventos;
}