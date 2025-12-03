package eventos.piura.controller;

import eventos.piura.services.impl.ManualPdfService;
import eventos.piura.services.impl.ManualPdfService.ManualMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ManualController {

    private final ManualPdfService manualPdfService;

    @GetMapping("/manual")
    public String verManual(Model model) {
        ManualMeta meta = manualPdfService.buildMeta();
        model.addAttribute("meta", meta);
        return "manual";
    }

    @GetMapping(value = "/manual.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarManual() {
        ManualMeta meta = manualPdfService.buildMeta();
        byte[] pdf = manualPdfService.renderPdf(meta);
        String filename = "Manual-" + meta.appName().replaceAll("\\s+", "-") + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
