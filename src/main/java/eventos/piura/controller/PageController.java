package eventos.piura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PageController {

    @GetMapping("/perfil")
    public String perfil(Model model) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nombre", "Carlos");
        usuario.put("apellido", "Rodríguez");
        usuario.put("username", "carlosrod");
        usuario.put("correo", "carlos.rodriguez@email.com");
        usuario.put("telefono", "987654321");
        usuario.put("creadoEn", "enero de 2024");
        usuario.put("estado", "ACTIVO");

        model.addAttribute("usuario", usuario);
        model.addAttribute("nivelActual", 5);
        model.addAttribute("puntosActuales", 2450);
        model.addAttribute("progresoPct", 82);
        model.addAttribute("puntosRestantes", 550);
        model.addAttribute("stats", Map.of(
                "eventosAsistidos", 12,
                "resenas", 8,
                "ordenes", 15,
                "entradasActivas", 3));
        model.addAttribute("billeteraSaldo", 150.00);

        // ▼ Reemplaza los Map.of(...) por HashMap
        Map<String, Object> e1 = new HashMap<>();
        e1.put("titulo", "Festival de Música");
        e1.put("categoria", "Música");
        e1.put("fecha", "15 mar 2025, 15:00");
        e1.put("checkIn", "15 mar 2025, 14:45");
        e1.put("imgUrl", null); // permitido en HashMap

        Map<String, Object> e2 = new HashMap<>();
        e2.put("titulo", "Conferencia Tech");
        e2.put("categoria", "Tecnología");
        e2.put("fecha", "20 mar 2025, 09:00");
        e2.put("checkIn", "20 mar 2025, 08:45");
        e2.put("imgUrl", null); // permitido en HashMap

        model.addAttribute("eventos", List.of(e1, e2));

        model.addAttribute("resenas", List.of(
                Map.of(
                        "tituloEvento", "Festival de Música Electrónica 2025",
                        "htmlEstrellas",
                        "<i class='bi bi-star-fill'></i><i class='bi bi-star-fill'></i><i class='bi bi-star-fill'></i><i class='bi bi-star-fill'></i><i class='bi bi-star-fill'></i>",
                        "fecha", "16 mar 2025",
                        "comentario", "Increíble experiencia, excelente organización y lineup espectacular.")));
        model.addAttribute("txs", List.of(
                Map.of("tipo", "CR", "concepto", "Recarga", "fecha", "20 mar, 09:30", "monto", "+S/ 100.00")));
        model.addAttribute("ordenes", List.of(
                Map.of("id", "123", "tituloEvento", "Festival de Música Electrónica 2025", "fecha", "10 feb", "total",
                        "S/ 250.00")));

        return "perfil/perfil";
    }

}