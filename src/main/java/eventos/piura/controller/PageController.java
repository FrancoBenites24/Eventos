package eventos.piura.controller;

import eventos.piura.dto.PerfilEventoView;
import eventos.piura.dto.PerfilOrdenView;
import eventos.piura.dto.PerfilTransaccionView;
import eventos.piura.dto.UsuarioResumenView;
import eventos.piura.mapper.UsuarioViewMapper;
import eventos.piura.model.Billetera;
import eventos.piura.model.Entrada;
import eventos.piura.model.Evento;
import eventos.piura.model.Orden;
import eventos.piura.model.WalletTx;
import eventos.piura.model.enums.EstadoEntrada;
import eventos.piura.model.Usuario;
import eventos.piura.model.enums.EstadoOrden;
import eventos.piura.model.enums.MetodoPago;
import eventos.piura.repository.BilleteraRepository;
import eventos.piura.repository.EntradaRepository;
import eventos.piura.repository.OrdenRepository;
import eventos.piura.repository.UsuarioRepository;
import eventos.piura.repository.WalletTxRepository;
import eventos.piura.services.EventoImagenService;
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
import java.util.*;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioViewMapper usuarioViewMapper;
    private final OrdenRepository ordenRepository;
    private final EntradaRepository entradaRepository;
    private final BilleteraRepository billeteraRepository;
    private final WalletTxRepository walletTxRepository;
    private final EventoImagenService eventoImagenService;

    @Value("${app.currency:PEN}")
    private String currency;

    private static final Locale LOCALE_ES_PE = new Locale("es", "PE");
    private static final DateTimeFormatter ORDEN_FECHA_FORMATTER =
            DateTimeFormatter.ofPattern("d MMM yyyy", LOCALE_ES_PE);
    private static final DateTimeFormatter EVENTO_FECHA_FORMATTER =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", LOCALE_ES_PE);
    private static final DateTimeFormatter TX_FECHA_FORMATTER =
            DateTimeFormatter.ofPattern("d MMM, HH:mm", LOCALE_ES_PE);

    @GetMapping("/perfil")
    public String perfil(Model model, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Usuario usuarioEntidad = usuarioRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UsuarioResumenView usuario = usuarioViewMapper.mapear(usuarioEntidad);
        UUID usuarioId = usuarioEntidad.getId();
        OffsetDateTime ahora = OffsetDateTime.now();

        List<PerfilOrdenView> ordenes = ordenRepository
                .findTop5ByCompradorIdOrderByCreadoEnDesc(usuarioId)
                .stream()
                .map(this::mapOrden)
                .toList();

        long totalOrdenes = ordenRepository.countByCompradorId(usuarioId);
        long eventosAsistidos = entradaRepository.countEventosAsistidos(usuarioId, EstadoEntrada.USADA);
        long entradasActivas = entradaRepository.countEntradasActivas(usuarioId, EstadoEntrada.EMITIDA, ahora);
        long totalGastadoCentavos = ordenRepository
                .sumTotalCentavosByCompradorIdAndEstado(usuarioId, EstadoOrden.PAGADA);

        Gamificacion gamificacion = calcularGamificacion(totalGastadoCentavos);

        Optional<Billetera> billeteraOpt = billeteraRepository.findByUsuarioId(usuarioId);
        double billeteraSaldo = billeteraOpt
                .map(b -> walletTxRepository.calcularSaldoCentavos(b.getId()) / 100.0)
                .orElse(0.0);
        List<PerfilTransaccionView> transacciones = billeteraOpt
                .map(this::mapearTransacciones)
                .orElse(List.of());

        List<PerfilEventoView> historialEventos = construirHistorialEventos(usuarioId);
        if (historialEventos.isEmpty()) {
            historialEventos = construirProximosEventos(usuarioId, ahora);
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("nivelActual", gamificacion.nivel());
        model.addAttribute("puntosActuales", gamificacion.puntos());
        model.addAttribute("progresoPct", gamificacion.progresoPct());
        model.addAttribute("puntosRestantes", gamificacion.puntosRestantes());
        model.addAttribute("stats", Map.of(
                "eventosAsistidos", eventosAsistidos,
                "resenas", 0L,
                "ordenes", totalOrdenes,
                "entradasActivas", entradasActivas));
        model.addAttribute("billeteraSaldo", billeteraSaldo);
        model.addAttribute("eventos", historialEventos);
        model.addAttribute("resenas", Collections.emptyList());
        model.addAttribute("txs", transacciones);
        model.addAttribute("ordenes", ordenes);

        return "perfil/perfil";
    }

    private Gamificacion calcularGamificacion(long totalGastadoCentavos) {
        int puntos = (int) (totalGastadoCentavos / 100);
        int puntosPorNivel = 500;
        int nivel = puntos / puntosPorNivel;
        int puntosRestantes = Math.max(0, ((nivel + 1) * puntosPorNivel) - puntos);
        int progreso = puntosPorNivel == 0
                ? 0
                : Math.min(100, Math.round(((float) (puntos % puntosPorNivel) / puntosPorNivel) * 100));
        return new Gamificacion(nivel, puntos, progreso, puntosRestantes);
    }

    private List<PerfilTransaccionView> mapearTransacciones(Billetera billetera) {
        return walletTxRepository.findTop5ByBilleteraIdOrderByCreadoEnDesc(billetera.getId())
                .stream()
                .map(this::mapearTransaccion)
                .toList();
    }

    private PerfilTransaccionView mapearTransaccion(WalletTx tx) {
        return new PerfilTransaccionView(
                tx.getId(),
                tx.getTipo(),
                traducirConcepto(tx.getConcepto()),
                formatearFechaTransaccion(tx.getCreadoEn()),
                formatearMontoTransaccion(tx)
        );
    }

    private String formatearFechaTransaccion(OffsetDateTime fecha) {
        if (fecha == null) {
            return "-";
        }
        return TX_FECHA_FORMATTER.format(fecha);
    }

    private String formatearMontoTransaccion(WalletTx tx) {
        String monto = formatearMoneda(tx.getMontoCentavos());
        return (esCredito(tx) ? "+" : "-") + monto;
    }

    private boolean esCredito(WalletTx tx) {
        return tx != null && "CR".equalsIgnoreCase(tx.getTipo());
    }

    private String traducirConcepto(String concepto) {
        if (concepto == null || concepto.isBlank()) {
            return "Movimiento";
        }
        String upper = concepto.toUpperCase(Locale.ROOT);
        return switch (upper) {
            case "CASHIN" -> "Recarga";
            case "CASHOUT" -> "Retiro";
            case "VENTA_ENTRADA" -> "Compra de entradas";
            case "AJUSTE" -> "Ajuste";
            case "COMISION_PLATAFORMA" -> "Comisión";
            default -> concepto.replace('_', ' ').toLowerCase(LOCALE_ES_PE);
        };
    }

    private List<PerfilEventoView> construirHistorialEventos(UUID usuarioId) {
        List<Entrada> entradas = entradaRepository.findEntradasPorEstado(usuarioId, EstadoEntrada.USADA);
        Map<UUID, PerfilEventoView> eventos = new LinkedHashMap<>();
        for (Entrada entrada : entradas) {
            Evento evento = entrada.getEvento();
            if (evento == null || evento.getId() == null) {
                continue;
            }
            eventos.putIfAbsent(evento.getId(), mapEvento(entrada, true));
            if (eventos.size() >= 6) {
                break;
            }
        }
        return List.copyOf(eventos.values());
    }

    private List<PerfilEventoView> construirProximosEventos(UUID usuarioId, OffsetDateTime referencia) {
        List<Entrada> entradas = entradaRepository.findEntradasUpcoming(usuarioId, EstadoEntrada.EMITIDA, referencia);
        Map<UUID, PerfilEventoView> eventos = new LinkedHashMap<>();
        for (Entrada entrada : entradas) {
            Evento evento = entrada.getEvento();
            if (evento == null || evento.getId() == null) {
                continue;
            }
            eventos.putIfAbsent(evento.getId(), mapEvento(entrada, false));
            if (eventos.size() >= 6) {
                break;
            }
        }
        return List.copyOf(eventos.values());
    }

    private PerfilEventoView mapEvento(Entrada entrada, boolean completado) {
        Evento evento = entrada.getEvento();
        UUID eventoId = evento != null ? evento.getId() : null;
        String titulo = safeTitulo(evento);
        String categoria = safeCategoria(evento);
        String fecha = formatearEventoFecha(evento != null ? evento.getInicioEn() : null);
        String checkIn = formatearCheckIn(entrada, completado);
        String imgUrl = resolverImagen(eventoId);
        return new PerfilEventoView(eventoId, titulo, categoria, fecha, checkIn, imgUrl);
    }

    private String formatearEventoFecha(OffsetDateTime fecha) {
        if (fecha == null) {
            return "-";
        }
        return EVENTO_FECHA_FORMATTER.format(fecha);
    }

    private String formatearCheckIn(Entrada entrada, boolean completado) {
        if (completado && entrada.getUsadaEn() != null) {
            return formatearEventoFecha(entrada.getUsadaEn());
        }
        return completado ? "No registrado" : "Disponible el día del evento";
    }

    private String safeCategoria(Evento evento) {
        if (evento != null && evento.getCategoria() != null) {
            String nombre = evento.getCategoria().getNombre();
            if (nombre != null && !nombre.isBlank()) {
                return nombre;
            }
        }
        return "General";
    }

    private String safeTitulo(Evento evento) {
        if (evento != null) {
            String titulo = evento.getTitulo();
            if (titulo != null && !titulo.isBlank()) {
                return titulo;
            }
        }
        return "Evento";
    }

    private String resolverImagen(UUID eventoId) {
        if (eventoId == null) {
            return "/img/placeholder-event.png";
        }
        return eventoImagenService.obtenerPrimeraImagen(eventoId)
                .map(eventoImagenService::construirUrl)
                .orElse("/img/placeholder-event.png");
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

    private record Gamificacion(int nivel, int puntos, int progresoPct, int puntosRestantes) {
    }
}
