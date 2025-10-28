package eventos.piura.controller;

import eventos.piura.dto.PerfilOrdenView;
import eventos.piura.dto.UsuarioResumenView;
import eventos.piura.mapper.UsuarioViewMapper;
import eventos.piura.model.Orden;
import eventos.piura.model.Usuario;
import eventos.piura.model.enums.EstadoOrden;
import eventos.piura.model.enums.MetodoPago;
import eventos.piura.repository.OrdenRepository;
import eventos.piura.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioViewMapper usuarioViewMapper;
    private final OrdenRepository ordenRepository;

    @Value("${app.currency:PEN}")
    private String currency;

    private static final Locale LOCALE_ES_PE = new Locale("es", "PE");
    private static final DateTimeFormatter ORDEN_FECHA_FORMATTER =
            DateTimeFormatter.ofPattern("d MMM yyyy", LOCALE_ES_PE);

    @GetMapping("/perfil")
    public String perfil(Model model, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Usuario usuarioEntidad = usuarioRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UsuarioResumenView usuario = usuarioViewMapper.mapear(usuarioEntidad);

        List<PerfilOrdenView> ordenes = ordenRepository
                .findTop5ByCompradorIdOrderByCreadoEnDesc(usuarioEntidad.getId())
                .stream()
                .map(this::mapOrden)
                .toList();

        long totalOrdenes = ordenRepository.countByCompradorId(usuarioEntidad.getId());

        model.addAttribute("usuario", usuario);
        model.addAttribute("nivelActual", 0);
        model.addAttribute("puntosActuales", 0);
        model.addAttribute("progresoPct", 0);
        model.addAttribute("puntosRestantes", 0);
        model.addAttribute("stats", Map.of(
                "eventosAsistidos", 0L,
                "resenas", 0L,
                "ordenes", totalOrdenes,
                "entradasActivas", 0L));
        model.addAttribute("billeteraSaldo", 0.0);
        model.addAttribute("eventos", Collections.emptyList());
        model.addAttribute("resenas", Collections.emptyList());
        model.addAttribute("txs", Collections.emptyList());
        model.addAttribute("ordenes", ordenes);

        return "perfil/perfil";
    }

    private PerfilOrdenView mapOrden(Orden orden) {
        String tituloEvento = orden.getEvento() != null ? orden.getEvento().getTitulo() : "Evento";
        String fecha = formatearFecha(orden.getCreadoEn());
        String total = formatearMoneda(orden.getTotalCentavos());
        String estadoEtiqueta = estadoEtiqueta(orden.getEstado());
        String estadoClase = estadoClase(orden.getEstado());
        String metodoPago = metodoPagoEtiqueta(orden.getMetodoPago());

        return new PerfilOrdenView(
                orden.getId(),
                tituloEvento,
                fecha,
                total,
                estadoEtiqueta,
                estadoClase,
                metodoPago
        );
    }

    private String formatearFecha(OffsetDateTime fecha) {
        if (fecha == null) {
            return "-";
        }
        return ORDEN_FECHA_FORMATTER.format(fecha);
    }

    private String formatearMoneda(Integer centavos) {
        int valor = centavos != null ? centavos : 0;
        BigDecimal monto = BigDecimal.valueOf(valor)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(LOCALE_ES_PE);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00", symbols);
        return currencySymbol() + " " + decimalFormat.format(monto);
    }

    private String estadoEtiqueta(EstadoOrden estado) {
        if (estado == null) {
            return "Pendiente";
        }
        return switch (estado) {
            case PAGADA -> "Pagada";
            case PENDIENTE -> "Pendiente";
            case FALLIDA -> "Fallida";
            case REEMBOLSADA -> "Reembolsada";
        };
    }

    private String estadoClase(EstadoOrden estado) {
        if (estado == null) {
            return " bg-warning-subtle text-warning";
        }
        return switch (estado) {
            case PAGADA -> " bg-success-subtle text-success";
            case PENDIENTE -> " bg-warning-subtle text-warning";
            case FALLIDA -> " bg-danger-subtle text-danger";
            case REEMBOLSADA -> " bg-info-subtle text-info";
        };
    }

    private String metodoPagoEtiqueta(MetodoPago metodoPago) {
        if (metodoPago == null) {
            return "Metodo desconocido";
        }
        return switch (metodoPago) {
            case YAPE -> "Yape";
            case PLIN -> "Plin";
            case TARJETA -> "Tarjeta";
            case BILLETERA -> "Billetera";
        };
    }

    private String currencySymbol() {
        if ("PEN".equalsIgnoreCase(currency) || "S/".equalsIgnoreCase(currency)) {
            return "S/";
        }
        if ("USD".equalsIgnoreCase(currency) || "$".equals(currency)) {
            return "$";
        }
        return currency != null ? currency.toUpperCase(Locale.ROOT) : "S/";
    }
}
