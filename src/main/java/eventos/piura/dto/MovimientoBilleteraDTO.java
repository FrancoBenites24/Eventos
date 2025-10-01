package eventos.piura.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MovimientoBilleteraDTO {
    private Long id;
    private Double monto;
    private String tipo; // recarga, compra, cashback, reembolso
    private String descripcion;
    private LocalDateTime creadoEn;
}