package eventos.piura.controller;

import eventos.piura.dto.carrito.CarritoResumenView;
import eventos.piura.services.CarritoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.UUID;

@Controller
@Validated
@RequestMapping("/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @PostMapping(
            value = "/agregar",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Map<String, Object> agregar(
            @RequestParam("entradaTipoId") UUID entradaTipoId,
            @RequestParam(name = "cantidad", defaultValue = "1") int cantidad
    ) {
        try {
            CarritoResumenView resumen = carritoService.agregar(entradaTipoId, cantidad);
            return Map.of(
                    "count", resumen.totalCantidad(),
                    "totalCentavos", resumen.totalCentavos()
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PostMapping(value = "/eliminar", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> eliminar(@RequestParam("entradaTipoId") UUID entradaTipoId) {
        try {
            CarritoResumenView resumen = carritoService.eliminar(entradaTipoId);
            return Map.of(
                    "count", resumen.totalCantidad(),
                    "totalCentavos", resumen.totalCentavos()
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PostMapping(value = "/limpiar", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> limpiar() {
        try {
            CarritoResumenView resumen = carritoService.limpiar();
            return Map.of(
                    "count", resumen.totalCantidad(),
                    "totalCentavos", resumen.totalCentavos()
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @GetMapping("/panel")
    public String panel(Model model) {
        CarritoResumenView resumen = carritoService.obtenerResumen();
        model.addAttribute("cart", resumen);
        return "fragments/_offcanvas-cart :: offcanvasCart";
    }
}
