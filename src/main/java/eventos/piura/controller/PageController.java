 package eventos.piura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Controller
public class PageController {

    // Perfil (sin cambios)
    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/confirmacion")
    public String confirmacion(
            @RequestParam(value = "metodo", required = false) String metodo,
            Model model) {

        model.addAttribute("codigo", "ORD-2025-000123");
        model.addAttribute("nombre", "Rubi Silva");
        model.addAttribute("correo", "rubi@example.com");
        model.addAttribute("total", new BigDecimal("150.80").setScale(2, RoundingMode.HALF_UP));
        model.addAttribute("mensaje", "¡Tu compra se realizó con éxito!");
        model.addAttribute("moneda", "S/"); // usa la misma moneda que en carrito
        model.addAttribute("metodo", (metodo != null && !metodo.isBlank()) ? metodo : "No especificado");

        return "confirmacion"; // ↔ templates/confirmacion.html
    }

    // ==========================
    // CARRITO (dinámico servidor)
    // ==========================
    @GetMapping("/carrito")
    public String carrito(Model model) {
        // Items de ejemplo (sin models)
        Map<String,Object> i1 = Map.of(
                "titulo", "Entrada Concierto",
                "lugar", "Coliseo Piura",
                "fecha", "2025-10-12 20:00",
                "precio", new BigDecimal("45.00"),
                "cantidad", 2,
                "importe", new BigDecimal("45.00").multiply(new BigDecimal("2"))
        );
        Map<String,Object> i2 = Map.of(
                "titulo", "Conf. Tech",
                "lugar", "Auditorio UDEP",
                "fecha", "2025-10-20 09:00",
                "precio", new BigDecimal("60.00"),
                "cantidad", 1,
                "importe", new BigDecimal("60.00").multiply(new BigDecimal("1"))
        );

        List<Map<String,Object>> items = List.of(i1, i2);

        // Totales con redondeo
        BigDecimal subtotal = items.stream()
                .map(m -> (BigDecimal) m.get("importe"))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal igv = subtotal.multiply(new BigDecimal("0.18"))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.add(igv)
                .setScale(2, RoundingMode.HALF_UP);

        // Atributos para Thymeleaf
        model.addAttribute("items", items);
        model.addAttribute("itemsCount", items.size());
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("igv", igv);
        model.addAttribute("total", total);
        model.addAttribute("moneda", "S/");
        model.addAttribute("metodosPago", List.of("Tarjeta", "Yape/Plin", "Transferencia"));

        return "DetalleCarrito"; // ↔ templates/DetalleCarrito.html
    }

}
