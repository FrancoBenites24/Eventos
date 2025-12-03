package eventos.piura.services.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import eventos.piura.dto.checkout.BoletaView;
import eventos.piura.dto.checkout.TicketDigitalView;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Component
public class TicketPdfGenerator {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.forLanguageTag("es-PE"));

    private final SpringTemplateEngine templateEngine;

    public TicketPdfGenerator(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generarPdf(BoletaView boleta, List<TicketDigitalView> tickets) {
        List<TicketView> ticketViews = tickets.stream()
                .map(ticket -> new TicketView(ticket, generarQrBase64(ticket.qrPayload())))
                .collect(Collectors.toList());

        Context context = new Context(Locale.forLanguageTag("es-PE"));
        context.setVariable("boleta", boleta);
        context.setVariable("tickets", ticketViews);

        String html = templateEngine.process("tickets/ticket-pdf", context);

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF de tickets", e);
        }
    }

    private String generarQrBase64(String payload) {
        try {
            BitMatrix matrix = new com.google.zxing.qrcode.QRCodeWriter()
                    .encode(payload, BarcodeFormat.QR_CODE, 300, 300);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", output);
                return Base64.getEncoder().encodeToString(output.toByteArray());
            }
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("No se pudo generar el código QR", e);
        }
    }

    private record TicketView(
            TicketDigitalView data,
            String qrImageBase64
    ) {
        String eventoTitulo() { return data.eventoTitulo(); }
        java.time.OffsetDateTime eventoFecha() { return data.eventoFecha(); }
        String eventoLugar() { return data.eventoLugar(); }
        String tipoNombre() { return data.tipoNombre(); }
        String nombreTitular() { return data.nombreTitular(); }
        String codigo() { return data.codigo(); }
        String boletaCodigo() { return data.boletaCodigo(); }
        java.util.UUID ordenId() { return data.ordenId(); }
        java.util.UUID ordenItemId() { return data.ordenItemId(); }
        String qrImage() { return qrImageBase64; }
    }
}
