package eventos.piura.services;

import eventos.piura.dto.checkout.BoletaView;
import eventos.piura.dto.checkout.TicketDigitalView;
import eventos.piura.dto.checkout.CheckoutPagoRequest;
import eventos.piura.model.Usuario;

import java.util.List;

public interface ConfirmacionCompraService {

    void enviarConfirmacion(Usuario comprador,
                            CheckoutPagoRequest request,
                            BoletaView boleta,
                            List<TicketDigitalView> tickets);
}
