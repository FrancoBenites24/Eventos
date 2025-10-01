package eventos.piura.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventoDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String direccion;
    private String distrito;
    private String ciudad;
    private Boolean esGratuito;
    private String banner;
}