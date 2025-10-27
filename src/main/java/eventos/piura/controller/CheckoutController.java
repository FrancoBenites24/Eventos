package eventos.piura.controller;

import eventos.piura.dto.checkout.BoletaView;
import eventos.piura.dto.checkout.CheckoutPagoRequest;
import eventos.piura.dto.checkout.CheckoutResumenView;
import eventos.piura.model.Usuario;
import eventos.piura.repository.UsuarioRepository;
import eventos.piura.services.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private final UsuarioRepository usuarioRepository;
    private final CheckoutService checkoutService;

    @GetMapping("/checkout")
    public String mostrarCheckout(Model model,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuario(authentication)
                .orElse(null);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesion para continuar con la compra.");
            return "redirect:/login";
        }

        CheckoutResumenView resumen = checkoutService.construirResumen(usuario);
        if (resumen.items().isEmpty()) {
            redirectAttributes.addFlashAttribute("info", "Tu carrito esta vacio, agrega entradas para continuar.");
            return "redirect:/eventos";
        }

        if (!model.containsAttribute("pago")) {
            CheckoutPagoRequest formulario = new CheckoutPagoRequest();
            formulario.setNombreCompleto(usuario.getNombre() + " " + usuario.getApellido());
            formulario.setCorreoElectronico(usuario.getCorreo());
            model.addAttribute("pago", formulario);
        }

        model.addAttribute("resumen", resumen);
        model.addAttribute("monedaSimbolo", resumen.moneda());
        return "checkout";
    }

    @PostMapping("/checkout/pagar")
    public String procesarPago(@Valid @ModelAttribute("pago") CheckoutPagoRequest request,
                               BindingResult bindingResult,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuario(authentication)
                .orElse(null);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesion para continuar con la compra.");
            return "redirect:/login";
        }

        CheckoutResumenView resumenActual = checkoutService.construirResumen(usuario);
        if (resumenActual.items().isEmpty()) {
            redirectAttributes.addFlashAttribute("info", "Tu carrito esta vacio.");
            return "redirect:/eventos";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("resumen", resumenActual);
            model.addAttribute("monedaSimbolo", resumenActual.moneda());
            return "checkout";
        }

        try {
            BoletaView boleta = checkoutService.procesarPago(usuario, request);
            model.addAttribute("boleta", boleta);
            return "confirmacion";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.reject("pago", ex.getMessage());
        }

        model.addAttribute("resumen", resumenActual);
        model.addAttribute("monedaSimbolo", resumenActual.moneda());
        return "checkout";
    }

    @GetMapping("/carrito")
    public String irAlCheckout() {
        return "redirect:/checkout";
    }

    private Optional<Usuario> obtenerUsuario(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByUsernameIgnoreCase(authentication.getName());
    }
}
