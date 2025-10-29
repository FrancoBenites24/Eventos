package eventos.piura.services;

import eventos.piura.dto.organizador.NotificationDTO;
import eventos.piura.dto.organizador.OrganizacionDTO;
import eventos.piura.dto.organizador.PaymentMethodDTO;
import eventos.piura.dto.organizador.FacturaDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizadorConfigService {
    Optional<OrganizacionDTO> readOrganizacion(UUID usuarioId) throws IOException;
    void writeOrganizacion(UUID usuarioId, OrganizacionDTO organizacion) throws IOException;

    List<NotificationDTO> readNotificaciones(UUID usuarioId) throws IOException;
    void pushNotificacion(UUID usuarioId, NotificationDTO notification) throws IOException;

    List<PaymentMethodDTO> readMetodosPago(UUID usuarioId) throws IOException;
    void addMetodoPago(UUID usuarioId, PaymentMethodDTO metodo) throws IOException;

    Optional<String> guardarImagenPerfil(UUID usuarioId, String originalFilename, byte[] bytes) throws IOException;
    // notificaciones
    void updateNotificacion(UUID usuarioId, NotificationDTO notification) throws IOException;
    void deleteNotificacion(UUID usuarioId, String notificationId) throws IOException;

    // facturacion / facturas
    List<FacturaDTO> readFacturas(UUID usuarioId) throws IOException;
    void addFactura(UUID usuarioId, FacturaDTO factura) throws IOException;
    Optional<String> guardarFacturaFile(UUID usuarioId, String originalFilename, byte[] bytes) throws IOException;
}
