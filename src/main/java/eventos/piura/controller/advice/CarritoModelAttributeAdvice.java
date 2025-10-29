package eventos.piura.controller.advice;

import eventos.piura.dto.carrito.CarritoResumenView;
import eventos.piura.services.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class CarritoModelAttributeAdvice {

    private final CarritoService carritoService;

    @ModelAttribute("cart")
    public CarritoResumenView carritoActual() {
        return carritoService.obtenerResumen();
    }
}
