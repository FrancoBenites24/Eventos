package eventos.piura.services.carrito;

import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CarritoSession {

    private final Map<UUID, CarritoSessionItem> items = new LinkedHashMap<>();

    public Map<UUID, CarritoSessionItem> getItems() {
        return items;
    }

    public record CarritoSessionItem(
            UUID eventoId,
            UUID entradaId,
            String titulo,
            String tipoNombre,
            int precioCentavos,
            int cantidad
    ) {
        public int subtotal() {
            return precioCentavos * cantidad;
        }

        public CarritoSessionItem incrementar(int delta) {
            return new CarritoSessionItem(eventoId, entradaId, titulo, tipoNombre, precioCentavos, cantidad + delta);
        }
    }
}
