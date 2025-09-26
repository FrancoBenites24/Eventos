package eventos.piura.controller;

import eventos.piura.model.Evento;
import eventos.piura.model.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class EventoController {

    // Dummy organizer for demo events
    private final Usuario dummyOrganizador = new Usuario(
        null,
        "demo_org",
        "Demo",
        "Organizador",
        "demo@eventos.piura",
        "password123",
        "123456789",
        null,
        LocalDateTime.now(),
        null
    );

    // Lista fija de eventos (15 en total)
    private final List<Evento> eventos = List.of(
        new Evento(1L, "Concierto Rock", "Noche de rock en Piura",
            LocalDateTime.parse("2025-10-05T18:00:00"),
            LocalDateTime.parse("2025-10-05T22:00:00"),
            "Plaza Principal, Piura",
            -5.1945,
            -80.6328,
            "Piura",
            "Piura",
            false,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        ),
        new Evento(2L, "Feria Gastronómica", "Comida típica del norte",
            LocalDateTime.parse("2025-10-12T10:00:00"),
            LocalDateTime.parse("2025-10-12T20:00:00"),
            "Mercado Central, Piura",
            -5.1950,
            -80.6350,
            "Piura",
            "Piura",
            true,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        ),
        new Evento(3L, "Maratón 10K", "Competencia deportiva en el malecón",
            LocalDateTime.parse("2025-11-01T07:00:00"),
            LocalDateTime.parse("2025-11-01T12:00:00"),
            "Malecón Grau, Piura",
            -5.2000,
            -80.6300,
            "Piura",
            "Piura",
            true,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        ),
        new Evento(4L, "Festival de Teatro", "Obras locales e internacionales",
            LocalDateTime.parse("2025-11-08T19:00:00"),
            LocalDateTime.parse("2025-11-08T23:00:00"),
            "Teatro Municipal, Piura",
            -5.1930,
            -80.6330,
            "Piura",
            "Piura",
            false,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        ),
        new Evento(5L, "Expo Tecnología", "Nuevas tendencias en IA y robótica",
            LocalDateTime.parse("2025-11-15T09:00:00"),
            LocalDateTime.parse("2025-11-15T18:00:00"),
            "Centro de Convenciones, Piura",
            -5.1900,
            -80.6400,
            "Piura",
            "Piura",
            false,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        ),
        new Evento(6L, "Concurso de Danza", "Danzas folclóricas del Perú",
            LocalDateTime.parse("2025-11-20T20:00:00"),
            LocalDateTime.parse("2025-11-20T23:00:00"),
            "Coliseo Cerrado, Piura",
            -5.1800,
            -80.6200,
            "Piura",
            "Piura",
            false,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        ),
        new Evento(7L, "Cine al Aire Libre", "Películas clásicas en la plaza",
            LocalDateTime.parse("2025-11-25T20:00:00"),
            LocalDateTime.parse("2025-11-25T22:00:00"),
            "Plaza de Armas, Piura",
            -5.1945,
            -80.6328,
            "Piura",
            "Piura",
            true,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        ),
        new Evento(8L, "Concierto de Jazz", "Noche de improvisación musical",
            LocalDateTime.parse("2025-11-28T21:00:00"),
            LocalDateTime.parse("2025-11-29T00:00:00"),
            "Barrio Italiano, Piura",
            -5.1960,
            -80.6340,
            "Piura",
            "Piura",
            false,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        ),
        new Evento(9L, "Torneo de Ajedrez", "Competencia regional",
            LocalDateTime.parse("2025-12-01T09:00:00"),
            LocalDateTime.parse("2025-12-01T18:00:00"),
            "Biblioteca Municipal, Piura",
            -5.1950,
            -80.6310,
            "Piura",
            "Piura",
            true,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        ),
        new Evento(10L, "Festival Navideño", "Actividades familiares",
            LocalDateTime.parse("2025-12-10T17:00:00"),
            LocalDateTime.parse("2025-12-10T21:00:00"),
            "Plaza de Armas, Piura",
            -5.1945,
            -80.6328,
            "Piura",
            "Piura",
            true,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        ),
        new Evento(11L, "Carrera de Bicicletas", "Ruta Piura–Catacaos",
            LocalDateTime.parse("2025-12-15T08:00:00"),
            LocalDateTime.parse("2025-12-15T13:00:00"),
            "Ruta Piura-Catacaos",
            -5.1300,
            -80.6600,
            "Catacaos",
            "Piura",
            true,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        ),
        new Evento(12L, "Exposición de Arte", "Pintores piuranos contemporáneos",
            LocalDateTime.parse("2025-12-20T10:00:00"),
            LocalDateTime.parse("2025-12-20T19:00:00"),
            "Galería de Arte, Piura",
            -5.1940,
            -80.6330,
            "Piura",
            "Piura",
            false,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        ),
        new Evento(13L, "Festival de Comedia", "Stand up y humor local",
            LocalDateTime.parse("2025-12-28T20:00:00"),
            LocalDateTime.parse("2025-12-28T23:00:00"),
            "Teatro Municipal, Piura",
            -5.1930,
            -80.6330,
            "Piura",
            "Piura",
            false,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        ),
        new Evento(14L, "Noche de Salsa", "Con orquestas invitadas",
            LocalDateTime.parse("2026-01-05T22:00:00"),
            LocalDateTime.parse("2026-01-06T02:00:00"),
            "Discoteca Central, Piura",
            -5.1970,
            -80.6360,
            "Piura",
            "Piura",
            false,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        ),
        new Evento(15L, "Feria del Libro", "Autores nacionales",
            LocalDateTime.parse("2026-01-15T10:00:00"),
            LocalDateTime.parse("2026-01-15T20:00:00"),
            "Biblioteca Municipal, Piura",
            -5.1950,
            -80.6310,
            "Piura",
            "Piura",
            true,
            dummyOrganizador,
            null,
            LocalDateTime.now()
        )
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
