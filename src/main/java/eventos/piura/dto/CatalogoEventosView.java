package eventos.piura.dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record CatalogoEventosView(
        EventoResumenView destacado,
        Map<String, List<EventoResumenView>> porCategoria
) {
    public static CatalogoEventosView vacio() {
        return new CatalogoEventosView(null, Collections.emptyMap());
    }
}
