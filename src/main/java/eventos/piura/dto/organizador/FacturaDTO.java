package eventos.piura.dto.organizador;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FacturaDTO {
    private String id;
    private String periodo; // e.g., "Enero 2025"
    private String estado; // Pagado, Pendiente
    private String monto; // formato mostrado
    private String archivoPath; // ruta al PDF si existe
    private LocalDate creadoEn;
}
