package eventos.piura.controller;

import eventos.piura.model.EventoImagen;
import eventos.piura.services.EventoImagenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/media/eventos")
@RequiredArgsConstructor
public class EventoMediaController {

    private final EventoImagenService eventoImagenService;

    @GetMapping("/{imagenId}")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable UUID imagenId) {
        EventoImagen imagen = eventoImagenService.obtenerPorId(imagenId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagen.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imagen.getNombreArchivo() + "\"")
                .body(imagen.getDatos());
    }
}
