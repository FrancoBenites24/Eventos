package eventos.piura.dto.wallet;

import eventos.piura.dto.PerfilTransaccionView;
import eventos.piura.dto.checkout.MetodoPagoGuardadoView;

import java.math.BigDecimal;
import java.util.List;

public record WalletResumenView(
        int saldoCentavos,
        String moneda,
        List<PerfilTransaccionView> transacciones,
        List<MetodoPagoGuardadoView> metodosGuardados
) {
    public BigDecimal saldoMoneda() {
        return BigDecimal.valueOf(saldoCentavos).movePointLeft(2);
    }
}
