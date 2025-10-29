package eventos.piura.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eventos.piura.dto.organizador.NotificationDTO;
import eventos.piura.dto.organizador.OrganizacionDTO;
import eventos.piura.dto.organizador.PaymentMethodDTO;
import eventos.piura.dto.organizador.FacturaDTO;
import eventos.piura.services.OrganizadorConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrganizadorConfigServiceImpl implements OrganizadorConfigService {

    private final Path baseDir = Paths.get("data", "organizadores");
    private final Path imagesDir = Paths.get("data", "imagenes");
    private final ObjectMapper mapper = new ObjectMapper();

    public OrganizadorConfigServiceImpl() throws IOException {
        if (!Files.exists(baseDir)) Files.createDirectories(baseDir);
        if (!Files.exists(imagesDir)) Files.createDirectories(imagesDir);
    }

    private Path usuarioFile(UUID usuarioId) {
        return baseDir.resolve(usuarioId.toString() + ".json");
    }

    private ObjectNode readRoot(UUID usuarioId) throws IOException {
        Path file = usuarioFile(usuarioId);
        if (!Files.exists(file)) {
            ObjectNode root = mapper.createObjectNode();
            root.set("organizacion", mapper.createObjectNode());
            root.putArray("notificaciones");
            root.putArray("metodosPago");
            mapper.writeValue(file.toFile(), root);
            return root;
        }
        return (ObjectNode) mapper.readTree(file.toFile());
    }

    private void writeRoot(UUID usuarioId, ObjectNode root) throws IOException {
        mapper.writeValue(usuarioFile(usuarioId).toFile(), root);
    }

    @Override
    public Optional<OrganizacionDTO> readOrganizacion(UUID usuarioId) throws IOException {
        ObjectNode root = readRoot(usuarioId);
        if (root.has("organizacion")) {
            return Optional.ofNullable(mapper.treeToValue(root.get("organizacion"), OrganizacionDTO.class));
        }
        return Optional.empty();
    }

    @Override
    public void writeOrganizacion(UUID usuarioId, OrganizacionDTO organizacion) throws IOException {
        ObjectNode root = readRoot(usuarioId);
        root.set("organizacion", mapper.valueToTree(organizacion));
        writeRoot(usuarioId, root);
    }

    @Override
    public List<NotificationDTO> readNotificaciones(UUID usuarioId) throws IOException {
        ObjectNode root = readRoot(usuarioId);
        if (root.has("notificaciones")) {
            return mapper.convertValue(root.get("notificaciones"), mapper.getTypeFactory().constructCollectionType(List.class, NotificationDTO.class));
        }
        return new ArrayList<>();
    }

    @Override
    public void pushNotificacion(UUID usuarioId, NotificationDTO notification) throws IOException {
        ObjectNode root = readRoot(usuarioId);
        List<NotificationDTO> list = readNotificaciones(usuarioId);
        if (notification.getCreadoEn() == null) notification.setCreadoEn(OffsetDateTime.now());
        list.add(0, notification);
        root.set("notificaciones", mapper.valueToTree(list));
        writeRoot(usuarioId, root);
    }

    @Override
    public void updateNotificacion(UUID usuarioId, NotificationDTO notification) throws IOException {
        List<NotificationDTO> list = readNotificaciones(usuarioId);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(notification.getId())) {
                list.set(i, notification);
                ObjectNode root = readRoot(usuarioId);
                root.set("notificaciones", mapper.valueToTree(list));
                writeRoot(usuarioId, root);
                return;
            }
        }
    }

    @Override
    public void deleteNotificacion(UUID usuarioId, String notificationId) throws IOException {
        List<NotificationDTO> list = readNotificaciones(usuarioId);
        list.removeIf(n -> n.getId().equals(notificationId));
        ObjectNode root = readRoot(usuarioId);
        root.set("notificaciones", mapper.valueToTree(list));
        writeRoot(usuarioId, root);
    }

    @Override
    public List<FacturaDTO> readFacturas(UUID usuarioId) throws IOException {
        ObjectNode root = readRoot(usuarioId);
        if (root.has("facturas")) {
            return mapper.convertValue(root.get("facturas"), mapper.getTypeFactory().constructCollectionType(List.class, FacturaDTO.class));
        }
        return new ArrayList<>();
    }

    @Override
    public void addFactura(UUID usuarioId, FacturaDTO factura) throws IOException {
        ObjectNode root = readRoot(usuarioId);
        List<FacturaDTO> list = readFacturas(usuarioId);
        if (factura.getId() == null || factura.getId().isBlank()) factura.setId(UUID.randomUUID().toString());
        list.add(0, factura);
        root.set("facturas", mapper.valueToTree(list));
        writeRoot(usuarioId, root);
    }

    @Override
    public Optional<String> guardarFacturaFile(UUID usuarioId, String originalFilename, byte[] bytes) throws IOException {
        if (originalFilename == null) return Optional.empty();
        String ext = ".pdf";
        int idx = originalFilename.lastIndexOf('.');
        if (idx > 0) ext = originalFilename.substring(idx);
        Path dir = baseDir.resolve(usuarioId.toString()).resolve("invoices");
        if (!Files.exists(dir)) Files.createDirectories(dir);
        String filename = "factura_" + System.currentTimeMillis() + ext;
        Path dest = dir.resolve(filename);
        Files.write(dest, bytes);
        return Optional.of(dest.toString().replace("\\", "/"));
    }

    @Override
    public List<PaymentMethodDTO> readMetodosPago(UUID usuarioId) throws IOException {
        ObjectNode root = readRoot(usuarioId);
        if (root.has("metodosPago")) {
            return mapper.convertValue(root.get("metodosPago"), mapper.getTypeFactory().constructCollectionType(List.class, PaymentMethodDTO.class));
        }
        return new ArrayList<>();
    }

    @Override
    public void addMetodoPago(UUID usuarioId, PaymentMethodDTO metodo) throws IOException {
        ObjectNode root = readRoot(usuarioId);
        List<PaymentMethodDTO> list = readMetodosPago(usuarioId);
        if (!StringUtils.hasText(metodo.getId())) metodo.setId(UUID.randomUUID().toString());
        list.add(0, metodo);
        root.set("metodosPago", mapper.valueToTree(list));
        writeRoot(usuarioId, root);
    }

    @Override
    public Optional<String> guardarImagenPerfil(UUID usuarioId, String originalFilename, byte[] bytes) throws IOException {
        if (originalFilename == null) return Optional.empty();
        String ext = ".jpg";
        int idx = originalFilename.lastIndexOf('.');
        if (idx > 0) ext = originalFilename.substring(idx);
        String filename = "organizador_" + usuarioId.toString() + ext;
        Path dest = imagesDir.resolve(filename);
        Files.write(dest, bytes);
        return Optional.of(dest.toString().replace("\\", "/"));
    }
}
