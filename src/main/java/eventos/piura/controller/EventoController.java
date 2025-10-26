package eventos.piura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class EventoController {

    @GetMapping("/eventos")
    public String eventos(Model model) {
        // No hay evento destacado aún
        Map<String, Object> destacado = null;

        // No hay eventos por categoría aún
        Map<String, List<Map<String, Object>>> porCat = Map.of();

        model.addAttribute("destacado", destacado);
        model.addAttribute("porCat", porCat);

        return "eventos";
    }
}
