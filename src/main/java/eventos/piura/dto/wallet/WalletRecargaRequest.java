package eventos.piura.dto.wallet;

import eventos.piura.model.enums.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class WalletRecargaRequest {

    @NotNull
    @DecimalMin(value = "1.00", message = "El monto mínimo es 1.00")
    private BigDecimal monto;

    @NotNull
    private MetodoPago metodo;

    private UUID metodoGuardadoId;

    @Size(max = 20)
    private String telefono;

    @Size(max = 20)
    private String codigoOperacion;

    @Size(max = 19)
    private String tarjetaNumero;

    @Pattern(regexp = "^(?:|(0[1-9]|1[0-2])/\\d{2})$", message = "Formato MM/AA")
    private String tarjetaExpiracion;

    @Pattern(regexp = "^(?:|\\d{3,4})$", message = "CVV invalido")
    private String tarjetaCvv;

    @Size(max = 80)
    private String tarjetaTitular;

    private boolean guardarMetodo;

    @Size(max = 80)
    private String aliasMetodo;

    public String telefonoSanitizado() {
        return telefono != null ? telefono.trim() : null;
    }

    public String codigoOperacionSanitizado() {
        return codigoOperacion != null ? codigoOperacion.trim() : null;
    }

    public String tarjetaExpiracionSanitizada() {
        return tarjetaExpiracion != null ? tarjetaExpiracion.trim() : null;
    }

    public String tarjetaCvvSanitizado() {
        return tarjetaCvv != null ? tarjetaCvv.trim() : null;
    }

    public String tarjetaTitularSanitizado() {
        return tarjetaTitular != null ? tarjetaTitular.trim() : null;
    }

    public String tarjetaNumeroSanitizado() {
        return tarjetaNumero != null ? tarjetaNumero.trim() : null;
    }
}
