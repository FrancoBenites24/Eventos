package eventos.piura.controller;

import eventos.piura.model.Evento;
import eventos.piura.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    // Lista todos los eventos
    @GetMapping("/eventos")
    public String listarEventos(Model model) {
        model.addAttribute("eventos", eventoRepository.findAll());
        return "eventos1"; // corresponde a src/main/resources/templates/eventos1.html
    }

    // detalle de un evento según su ID
    @GetMapping("/detalle-evento/{id}")
    public String detalleEvento(@PathVariable Long id, Model model) {
        Evento evento = eventoRepository.findById(id).orElse(null);
        model.addAttribute("evento", evento);
        return "detalle-evento"; // corresponde a src/main/resources/templates/detalle-evento.html
    }
}
