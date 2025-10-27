package eventos.piura.services.impl;

import eventos.piura.model.Evento;
import eventos.piura.model.EventoImagen;
import eventos.piura.repository.EventoImagenRepository;
import eventos.piura.services.EventoImagenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventoImagenServiceImpl implements EventoImagenService {

    private static final int MAX_IMAGENES = 5;

    private final EventoImagenRepository eventoImagenRepository;

    @Override
    @Transactional
    public void asignarImagenes(Evento evento, List<MultipartFile> archivos) {
        List<MultipartFile> efectivos = filtrarArchivosValidos(archivos);

        if (efectivos.size() > MAX_IMAGENES) {
            throw new IllegalArgumentException("Puede registrar hasta " + MAX_IMAGENES + " imagenes por evento.");
        }

        evento.getImagenes().clear();

        int orden = 0;
        for (MultipartFile archivo : efectivos) {
            validarTipoContenido(archivo);
            EventoImagen imagen = construirImagen(evento, archivo, orden++);
            evento.getImagenes().add(imagen);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventoImagen> obtenerPrimeraImagen(UUID eventoId) {
        return eventoImagenRepository.findFirstByEventoIdOrderByOrdenAsc(eventoId);
    }

    @Override
    @Transactional(readOnly = true)
    public EventoImagen obtenerPorId(UUID imagenId) {
        return eventoImagenRepository.findById(imagenId)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada"));
    }

    @Override
    public String construirUrl(EventoImagen imagen) {
        return "/media/eventos/" + imagen.getId();
    }

    private List<MultipartFile> filtrarArchivosValidos(List<MultipartFile> archivos) {
        if (archivos == null || archivos.isEmpty()) {
            return List.of();
        }
        List<MultipartFile> filtrados = new ArrayList<>();
        for (MultipartFile archivo : archivos) {
            if (archivo != null && !archivo.isEmpty()) {
                filtrados.add(archivo);
            }
        }
        return filtrados;
    }

    private void validarTipoContenido(MultipartFile archivo) {
        String tipo = archivo.getContentType();
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El archivo " + archivo.getOriginalFilename() + " no tiene un tipo de contenido vÃ¡lido.");
        }

        String tipoNormalizado = tipo.toLowerCase();
        if (!tipoNormalizado.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo " + archivo.getOriginalFilename() + " debe ser una imagen.");
        }
    }

    private EventoImagen construirImagen(Evento evento, MultipartFile archivo, int orden) {
        EventoImagen imagen = new EventoImagen();
        imagen.setEvento(evento);
        imagen.setOrden(orden);
        imagen.setNombreArchivo(sanitizarNombre(archivo.getOriginalFilename()));
        imagen.setContentType(normalizarContentType(archivo.getContentType()));
        imagen.setTamanioBytes(archivo.getSize());
        try {
            imagen.setDatos(archivo.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo " + archivo.getOriginalFilename(), e);
        }
        return imagen;
    }

    private String sanitizarNombre(String originalFilename) {
        if (originalFilename == null) {
            return "imagen";
        }
        String limpio = StringUtils.cleanPath(originalFilename);
        return limpio.isBlank() ? "imagen" : limpio;
    }

    private String normalizarContentType(String contentType) {
        if (contentType == null) {
            return "application/octet-stream";
        }
        return contentType.toLowerCase();
    }
}
