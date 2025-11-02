package eventos.piura.services.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import eventos.piura.dto.checkout.BoletaItemView;
import eventos.piura.dto.checkout.BoletaView;
import eventos.piura.dto.checkout.TicketDigitalView;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@ConditionalOnClass(PDDocument.class)
@Component
public class TicketPdfGenerator {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.forLanguageTag("es-PE"));
    private static final DecimalFormat MONTO_FORMAT = new DecimalFormat("#,##0.00");
    private static final float MARGIN = 50f;

    public byte[] generarPdf(BoletaView boleta, List<TicketDigitalView> tickets) {
        try (PDDocument document = new PDDocument()) {
            agregarPaginaResumen(document, boleta, tickets.size());
            for (TicketDigitalView ticket : tickets) {
                agregarPaginaTicket(document, boleta, ticket);
            }
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                document.save(output);
                return output.toByteArray();
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el PDF de tickets", e);
        }
    }

    private void agregarPaginaResumen(PDDocument document, BoletaView boleta, int cantidadTickets) throws IOException {
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);

        try (PDPageContentStream content = new PDPageContentStream(document, page, AppendMode.APPEND, true, true)) {
            float y = page.getMediaBox().getHeight() - MARGIN;

            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 18);
            content.newLineAtOffset(MARGIN, y);
            content.showText("Resumen de compra");
            content.endText();
            y -= 30;

            y = escribirLinea(content, "Codigo de boleta: " + boleta.codigo(), y, PDType1Font.HELVETICA_BOLD, 12);
            y = escribirLinea(content, "Cliente: " + boleta.clienteNombre(), y, PDType1Font.HELVETICA, 12);
            y = escribirLinea(content, "Correo: " + boleta.clienteCorreo(), y, PDType1Font.HELVETICA, 12);
            y = escribirLinea(content, "Metodo de pago: " + boleta.metodoPago(), y, PDType1Font.HELVETICA, 12);
            if (boleta.descripcionPago() != null && !boleta.descripcionPago().isBlank()) {
                y = escribirLinea(content, "Referencia: " + boleta.descripcionPago(), y, PDType1Font.HELVETICA, 12);
            }
            y = escribirLinea(content, "Fecha de pago: " + FECHA_HORA.format(boleta.fechaPago()), y, PDType1Font.HELVETICA, 12);
            y = escribirLinea(content, "Entradas emitidas: " + cantidadTickets, y, PDType1Font.HELVETICA, 12);
            y -= 10;

            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            content.beginText();
            content.newLineAtOffset(MARGIN, y);
            content.showText("Detalle:");
            content.endText();
            y -= 18;

            for (BoletaItemView item : boleta.items()) {
                y = escribirLinea(content, "- " + item.eventoTitulo() + " / " + item.tipoNombre()
                        + " x" + item.cantidad(), y, PDType1Font.HELVETICA, 12);
            }
            y -= 20;

            y = escribirLinea(content, "Subtotal: " + formatearMonto(boleta.moneda(), boleta.subtotalMoneda().doubleValue()),
                    y, PDType1Font.HELVETICA_BOLD, 12);
            y = escribirLinea(content, "IGV: " + formatearMonto(boleta.moneda(), boleta.igvMoneda().doubleValue()),
                    y, PDType1Font.HELVETICA_BOLD, 12);
            escribirLinea(content, "Total: " + formatearMonto(boleta.moneda(), boleta.totalMoneda().doubleValue()),
                    y, PDType1Font.HELVETICA_BOLD, 14);
        }
    }

    private void agregarPaginaTicket(PDDocument document, BoletaView boleta, TicketDigitalView ticket) throws IOException {
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);

        try (PDPageContentStream content = new PDPageContentStream(document, page, AppendMode.APPEND, true, true)) {
            float y = page.getMediaBox().getHeight() - MARGIN;

            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 18);
            content.newLineAtOffset(MARGIN, y);
            content.showText("Entrada digital");
            content.endText();
            y -= 30;

            y = escribirLinea(content, "Evento: " + ticket.eventoTitulo(), y, PDType1Font.HELVETICA_BOLD, 14);
            y = escribirLinea(content, "Fecha y hora: " + FECHA_HORA.format(ticket.eventoFecha()), y, PDType1Font.HELVETICA, 12);
            y = escribirLinea(content, "Ubicacion: " + ticket.eventoLugar(), y, PDType1Font.HELVETICA, 12);
            y = escribirLinea(content, "Tipo de entrada: " + ticket.tipoNombre(), y, PDType1Font.HELVETICA, 12);
            y = escribirLinea(content, "Titular: " + ticket.nombreTitular(), y, PDType1Font.HELVETICA, 12);
            y = escribirLinea(content, "Codigo del ticket: " + ticket.codigo(), y, PDType1Font.HELVETICA_BOLD, 12);
            y = escribirLinea(content, "Codigo de boleta: " + ticket.boletaCodigo(), y, PDType1Font.HELVETICA, 12);
            y = escribirLinea(content, "Orden: " + ticket.ordenId(), y, PDType1Font.HELVETICA, 12);
            y -= 20;

            PDImageXObject qr = crearImagenQr(document, ticket.qrPayload());
            float qrSize = 200f;
            float qrX = page.getMediaBox().getWidth() - qrSize - MARGIN;
            float qrY = y - qrSize + 40;
            content.drawImage(qr, qrX, qrY, qrSize, qrSize);

            content.beginText();
            content.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
            content.newLineAtOffset(MARGIN, qrY - 20);
            content.showText("Presenta este codigo QR en el ingreso del evento.");
            content.endText();
        }
    }

    private float escribirLinea(PDPageContentStream content,
                                String texto,
                                float y,
                                PDType1Font font,
                                float fontSize) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(MARGIN, y);
        content.showText(texto);
        content.endText();
        return y - (fontSize + 6);
    }

    private String formatearMonto(String moneda, double valor) {
        return moneda + " " + MONTO_FORMAT.format(valor);
    }

    private PDImageXObject crearImagenQr(PDDocument document, String payload) throws IOException {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(payload, BarcodeFormat.QR_CODE, 300, 300);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            return LosslessFactory.createFromImage(document, image);
        } catch (WriterException e) {
            throw new IllegalStateException("No se pudo generar el codigo QR", e);
        }
    }
}
