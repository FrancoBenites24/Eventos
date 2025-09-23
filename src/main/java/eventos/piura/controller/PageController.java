package eventos.piura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/eventos")
    public String eventos() {
        return "eventos1";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/confirmacion")
    public String confirmacion() {
        return "confirmacion";
    }

    @GetMapping("/carrito")
    public String carrito() {
        return "DetalleCarrito";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "registro";
    }
}
