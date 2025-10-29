package eventos.piura.controller;

import eventos.piura.dto.wallet.WalletRecargaRequest;
import eventos.piura.dto.wallet.WalletResumenView;
import eventos.piura.model.Usuario;
import eventos.piura.repository.UsuarioRepository;
import eventos.piura.services.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.EnumSet;
import java.util.Optional;

@Controller
@RequestMapping("/billetera")
@RequiredArgsConstructor
public class WalletController {

    private final UsuarioRepository usuarioRepository;
    private final WalletService walletService;

    @GetMapping
    public String verBilletera(Model model,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuario(authentication)
                .orElse(null);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para acceder a tu billetera.");
            return "redirect:/login";
        }

        if (!model.containsAttribute("recarga")) {
            model.addAttribute("recarga", new WalletRecargaRequest());
        }

        WalletResumenView resumen = walletService.obtenerResumen(usuario);

        model.addAttribute("resumen", resumen);
        model.addAttribute("metodos", EnumSet.of(
                eventos.piura.model.enums.MetodoPago.YAPE,
                eventos.piura.model.enums.MetodoPago.PLIN,
                eventos.piura.model.enums.MetodoPago.TARJETA
        ));
        return "billetera";
    }

    @PostMapping("/recargar")
    public String recargar(@Valid @ModelAttribute("recarga") WalletRecargaRequest request,
                           BindingResult bindingResult,
                           Authentication authentication,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuario(authentication)
                .orElse(null);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para continuar.");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            WalletResumenView resumen = walletService.obtenerResumen(usuario);
            model.addAttribute("resumen", resumen);
            model.addAttribute("metodos", EnumSet.of(
                    eventos.piura.model.enums.MetodoPago.YAPE,
                    eventos.piura.model.enums.MetodoPago.PLIN,
                    eventos.piura.model.enums.MetodoPago.TARJETA
            ));
            return "billetera";
        }

        try {
            walletService.recargar(usuario, request);
            redirectAttributes.addFlashAttribute("success", "Recarga realizada correctamente.");
            return "redirect:/billetera";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.reject("recarga", ex.getMessage());
        }

        WalletResumenView resumen = walletService.obtenerResumen(usuario);
        model.addAttribute("resumen", resumen);
        model.addAttribute("metodos", EnumSet.of(
                eventos.piura.model.enums.MetodoPago.YAPE,
                eventos.piura.model.enums.MetodoPago.PLIN,
                eventos.piura.model.enums.MetodoPago.TARJETA
        ));
        return "billetera";
    }

    private Optional<Usuario> obtenerUsuario(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByUsernameIgnoreCase(authentication.getName());
    }
}
