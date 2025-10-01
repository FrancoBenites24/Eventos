package eventos.piura.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@SessionAttributes({"cart", "metodoPago", "moneda"})
public class PageController {

        @ModelAttribute("moneda")
        public String moneda() { return "S/"; }

        @ModelAttribute("metodosPago")
        public List<String> metodosPago() {
        return List.of("Tarjeta", "Yape/Plin", "Transferencia");
        }

        @ModelAttribute("cart")
        public List<Map<String,Object>> initCart() {
        Map<String,Object> i1 = new HashMap<>();
        i1.put("id", 1L);
        i1.put("titulo", "Entrada Concierto");
        i1.put("lugar", "Coliseo Piura");
        i1.put("fecha", "2025-10-12 20:00");
        i1.put("precio", new BigDecimal("45.00"));
        i1.put("cantidad", 2);

        Map<String,Object> i2 = new HashMap<>();
        i2.put("id", 2L);
        i2.put("titulo", "Conf. Tech");
        i2.put("lugar", "Auditorio UDEP");
        i2.put("fecha", "2025-10-20 09:00");
        i2.put("precio", new BigDecimal("60.00"));
        i2.put("cantidad", 1);

        return new ArrayList<>(List.of(i1, i2));
        }

        private void calcularTotales(Model model, List<Map<String,Object>> cart) {
        cart.forEach(m -> {
        BigDecimal precio = (BigDecimal) m.get("precio");
        int cantidad = (int) m.get("cantidad");
        m.put("importe", precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP));
        });

        BigDecimal subtotal = cart.stream()
                .map(m -> (BigDecimal) m.get("importe"))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal igv = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv).setScale(2, RoundingMode.HALF_UP);

        model.addAttribute("items", cart);
        model.addAttribute("itemsCount", cart.size());
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("igv", igv);
        model.addAttribute("total", total);
        }

        @GetMapping("/carrito")
        public String carrito(Model model, @ModelAttribute("cart") List<Map<String,Object>> cart) {
        calcularTotales(model, cart);
        return "DetalleCarrito";
        }

        @GetMapping("/confirmacion")
        public String confirmacion(
        @SessionAttribute(value = "metodoPago", required = false) String metodo,
        @ModelAttribute("cart") List<Map<String,Object>> cart,
        Model model
        ) {
        calcularTotales(model, cart);
        model.addAttribute("codigo", "ORD-2025-000123");
        model.addAttribute("nombre", "Rubi Silva");
        model.addAttribute("correo", "rubi@example.com");
        model.addAttribute("mensaje", "¡Tu compra se realizó con éxito!");
        model.addAttribute("metodo", (metodo != null && !metodo.isBlank()) ? metodo : "No especificado");
        return "confirmacion";
}

        @GetMapping("/profile") public String profile(){ return "profile"; }
        @PostMapping("/carrito/qty")
        public String actualizarCantidad(
        @RequestParam("id") Long id,
        @RequestParam("op") String op,
        @ModelAttribute("cart") List<Map<String,Object>> cart,
        RedirectAttributes ra
        ) {
        cart.stream().filter(m -> Objects.equals(m.get("id"), id)).findFirst().ifPresent(m -> {
        int q = (int) m.get("cantidad");
        if ("inc".equals(op)) q++;
        if ("dec".equals(op) && q > 1) q--;
        m.put("cantidad", q);
        });
        ra.addFlashAttribute("ok", "Cantidad actualizada");
        return "redirect:/carrito";
        }

        @PostMapping("/carrito/eliminar")
        public String eliminarItem(
        @RequestParam("id") Long id,
        @ModelAttribute("cart") List<Map<String,Object>> cart,
        RedirectAttributes ra
        ) {
        cart.removeIf(m -> Objects.equals(m.get("id"), id));
        ra.addFlashAttribute("ok", "Item eliminado");
        return "redirect:/carrito";
        } 

        @PostMapping("/carrito/pagar")
        public String pagar(@RequestParam("metodo") String metodo, Model model) {
        model.addAttribute("metodoPago", metodo);
        return "redirect:/confirmacion";
        }
}
