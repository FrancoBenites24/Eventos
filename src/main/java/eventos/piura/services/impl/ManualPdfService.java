package eventos.piura.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ManualPdfService {

    private static final Locale LOCALE_ES_PE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter FECHA_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM uuuu HH:mm z", LOCALE_ES_PE);

    private final SpringTemplateEngine templateEngine;

    @Value("${app.name:Eventos}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.timezone:America/Lima}")
    private String timezone;

    @Value("${app.currency:PEN}")
    private String currency;

    @Value("${app.reservation-timeout-minutes:15}")
    private int reservaMinutos;

    @Value("${spring.mail.username:no-reply@example.com}")
    private String mailFrom;

    public ManualMeta buildMeta() {
        String zona = (timezone != null && !timezone.isBlank()) ? timezone : "UTC";
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(zona);
        } catch (Exception ex) {
            zoneId = ZoneId.systemDefault();
            zona = zoneId.getId();
        }
        ZonedDateTime ahora = ZonedDateTime.now(zoneId);
        String generadoEn = FECHA_FORMATTER.format(ahora);
        return new ManualMeta(
                appName != null && !appName.isBlank() ? appName : "Eventos",
                appVersion != null && !appVersion.isBlank() ? appVersion : "1.0.0",
                generadoEn,
                zona,
                currency != null && !currency.isBlank() ? currency : "PEN",
                reservaMinutos,
                mailFrom != null && !mailFrom.isBlank() ? mailFrom : "no-reply@example.com"
        );
    }

    public String renderHtml() {
        return renderHtml(buildMeta());
    }

    public String renderHtml(ManualMeta meta) {
        Context context = new Context(LOCALE_ES_PE);
        context.setVariable("meta", meta);
        return templateEngine.process("manual", context);
    }

    public byte[] renderPdf() {
        return renderPdf(buildMeta());
    }

    public byte[] renderPdf(ManualMeta meta) {
        String html = renderHtml(meta);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF del manual", e);
        }
    }

    public record ManualMeta(
            String appName,
            String appVersion,
            String generadoEn,
            String timezone,
            String currency,
            int reservaMinutos,
            String mailFrom
    ) {
    }
}
