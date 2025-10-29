package eventos.piura.dto;

import java.util.List;
import java.util.Map;

public record PerfilPageData(
        UsuarioResumenView usuario,
        Gamificacion gamificacion,
        Map<String, Long> stats,
        double billeteraSaldo,
        List<PerfilEventoView> eventos,
        List<PerfilTransaccionView> transacciones,
        List<PerfilOrdenView> ordenes
) {

    public record Gamificacion(int nivel, int puntos, int progresoPct, int puntosRestantes) {
    }
}
