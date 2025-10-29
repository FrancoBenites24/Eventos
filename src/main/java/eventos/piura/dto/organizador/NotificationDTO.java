package eventos.piura.dto.organizador;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class NotificationDTO {
    private String id;
    private String tipo;
    private String titulo;
    private String mensaje;
    private String descripcion; // alias para la plantilla
    private Boolean activo = true;
    private OffsetDateTime creadoEn;
}
