package eventos.piura.controller;

import eventos.piura.model.Evento;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class EventoController {

    // Lista fija de eventos (15 en total)
    private final List<Evento> eventos = List.of(
        new Evento(1L, "Concierto Rock", "Noche de rock en Piura", "2025-10-05"),
        new Evento(2L, "Feria Gastronómica", "Comida típica del norte", "2025-10-12"),
        new Evento(3L, "Maratón 10K", "Competencia deportiva en el malecón", "2025-11-01"),
        new Evento(4L, "Festival de Teatro", "Obras locales e internacionales", "2025-11-08"),
        new Evento(5L, "Expo Tecnología", "Nuevas tendencias en IA y robótica", "2025-11-15"),
        new Evento(6L, "Concurso de Danza", "Danzas folclóricas del Perú", "2025-11-20"),
        new Evento(7L, "Cine al Aire Libre", "Películas clásicas en la plaza", "2025-11-25"),
        new Evento(8L, "Concierto de Jazz", "Noche de improvisación musical", "2025-11-28"),
        new Evento(9L, "Torneo de Ajedrez", "Competencia regional", "2025-12-01"),
        new Evento(10L, "Festival Navideño", "Actividades familiares", "2025-12-10"),
        new Evento(11L, "Carrera de Bicicletas", "Ruta Piura–Catacaos", "2025-12-15"),
        new Evento(12L, "Exposición de Arte", "Pintores piuranos contemporáneos", "2025-12-20"),
        new Evento(13L, "Festival de Comedia", "Stand up y humor local", "2025-12-28"),
        new Evento(14L, "Noche de Salsa", "Con orquestas invitadas", "2026-01-05"),
        new Evento(15L, "Feria del Libro", "Autores nacionales", "2026-01-15")
    );

    // Lista todos los eventos
    @GetMapping("/eventos")
    public String listarEventos(Model model) {
        model.addAttribute("eventos", eventos);
        return "eventos1"; // corresponde a src/main/resources/templates/eventos1.html
    }

    // detalle de un evento según su ID
    @GetMapping("/detalle-evento/{id}")
    public String detalleEvento(@PathVariable Long id, Model model) {
        Evento evento = eventos.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
        model.addAttribute("evento", evento);
        return "detalle-evento"; // corresponde a src/main/resources/templates/detalle-evento.html
    }
}
