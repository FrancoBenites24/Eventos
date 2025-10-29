package eventos.piura.dto.organizador;

import lombok.Data;

@Data
public class PaymentMethodDTO {
    private String id;
    private String tipo; // tarjeta, banco, paypal
    private String descripcion; // ejemplo: **** 4242 (VISA)
    private String datosEnmascarados; // info guardada mínima
}
