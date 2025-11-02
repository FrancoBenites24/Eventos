package eventos.piura.services.impl;

import eventos.piura.dto.checkout.BoletaView;
import eventos.piura.dto.checkout.CheckoutPagoRequest;
import eventos.piura.dto.checkout.TicketDigitalView;
import eventos.piura.model.Usuario;
import eventos.piura.repository.BoletoEntradaRepository;
import eventos.piura.services.ConfirmacionCompraService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@ConditionalOnBean(TicketPdfGenerator.class)
@RequiredArgsConstructor
@Slf4j
public class ConfirmacionCompraServiceImpl implements ConfirmacionCompraService {

    private final JavaMailSender mailSender;
    private final TicketPdfGenerator ticketPdfGenerator;
    private final BoletoEntradaRepository boletoEntradaRepository;

    @Value("${app.mail.from-name:Eventos}")
    private String fromName;

    @Value("${spring.mail.username:no-reply@example.com}")
    private String fromAddress;

    @Value("${app.mail.ticket-subject:Tus entradas digitales}")
    private String asunto;

    @Value("${app.mail.ticket-summary-intro:Adjuntamos tus entradas en PDF.}")
    private String mensajeIntro;

    @Override
    public void enviarConfirmacion(Usuario comprador,
                                   CheckoutPagoRequest request,
                                   BoletaView boleta,
                                   List<TicketDigitalView> tickets) {
        if (tickets.isEmpty()) {
            return;
        }
        try {
            byte[] pdf = ticketPdfGenerator.generarPdf(boleta, tickets);
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, StandardCharsets.UTF_8.name());
            helper.setTo(request.getCorreoElectronico());
            helper.setSubject(asunto);
            helper.setFrom(new InternetAddress(fromAddress, fromName));

            String cuerpo = construirCuerpoHtml(comprador, request, boleta, tickets);
            helper.setText(cuerpo, true);
            helper.addAttachment("Entradas-" + boleta.codigo() + ".pdf",
                    new ByteArrayResource(pdf), "application/pdf");

            mailSender.send(mensaje);

            OffsetDateTime enviado = OffsetDateTime.now();
            var boletos = boletoEntradaRepository.findAllById(
                    tickets.stream().map(TicketDigitalView::boletoId).toList()
            );
            boletos.forEach(boleto -> boleto.setEnviadoEn(enviado));
            boletoEntradaRepository.saveAll(boletos);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Fallo al enviar correo de confirmacion para el usuario {}", comprador.getCorreo(), e);
        } catch (RuntimeException ex) {
            log.error("No se pudo generar las entradas en PDF para el usuario {}", comprador.getCorreo(), ex);
        }
    }

    private String construirCuerpoHtml(Usuario comprador,
                                       CheckoutPagoRequest request,
                                       BoletaView boleta,
                                       List<TicketDigitalView> tickets) {
        return """
                <div>
                  <p>Hola %s,</p>
                  <p>%s</p>
                  <p><strong>Boleta:</strong> %s<br>
                  <strong>Metodo de pago:</strong> %s<br>
                  <strong>Total pagado:</strong> %s %.2f</p>
                  <p>Entradas generadas: %d</p>
                  <p>Gracias por tu compra.<br>Equipo Eventos Piura</p>
                </div>
                """.formatted(
                comprador.getNombre(),
                mensajeIntro,
                boleta.codigo(),
                boleta.metodoPago(),
                boleta.moneda(),
                boleta.totalMoneda().doubleValue(),
                tickets.size()
        );
    }
}
