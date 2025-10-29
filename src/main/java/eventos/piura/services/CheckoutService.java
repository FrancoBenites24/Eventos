package eventos.piura.services;

import eventos.piura.dto.checkout.BoletaView;
import eventos.piura.dto.checkout.CheckoutPagoRequest;
import eventos.piura.dto.checkout.CheckoutResumenView;
import eventos.piura.model.Usuario;

public interface CheckoutService {

    CheckoutResumenView construirResumen(Usuario usuario);

    BoletaView procesarPago(Usuario usuario, CheckoutPagoRequest request);
}
