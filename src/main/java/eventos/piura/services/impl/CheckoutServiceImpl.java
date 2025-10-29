package eventos.piura.services.impl;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eventos.piura.dto.checkout.BoletaItemView;
import eventos.piura.dto.checkout.BoletaView;
import eventos.piura.dto.checkout.CheckoutItemView;
import eventos.piura.dto.checkout.CheckoutPagoRequest;
import eventos.piura.dto.checkout.CheckoutResumenView;
import eventos.piura.dto.checkout.MetodoPagoGuardadoView;
import eventos.piura.model.Billetera;
import eventos.piura.model.Evento;
import eventos.piura.model.EventoEntradaTipo;
import eventos.piura.model.Orden;
import eventos.piura.model.MetodoPagoGuardado;
import eventos.piura.model.OrdenItem;
import eventos.piura.model.Usuario;
import eventos.piura.model.WalletTx;
import eventos.piura.model.enums.EstadoOrden;
import eventos.piura.model.enums.MetodoPago;
import eventos.piura.repository.BilleteraRepository;
import eventos.piura.repository.EventoEntradaTipoRepository;
import eventos.piura.repository.EventoRepository;
import eventos.piura.repository.OrdenRepository;
import eventos.piura.repository.WalletTxRepository;
import eventos.piura.services.CarritoService;
import eventos.piura.services.CheckoutService;
import eventos.piura.services.MetodoPagoGuardadoService;
import eventos.piura.services.carrito.CarritoSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckoutServiceImpl implements CheckoutService {

    private static final BigDecimal IGV_PORCENTAJE = new BigDecimal("0.18");

    private final EventoRepository eventoRepository;
    private final EventoEntradaTipoRepository eventoEntradaTipoRepository;
    private final OrdenRepository ordenRepository;
    private final CarritoService carritoService;
    private final CarritoSession carritoSession;
    private final BilleteraRepository billeteraRepository;
    private final WalletTxRepository walletTxRepository;
    private final MetodoPagoGuardadoService metodoPagoGuardadoService;

    @Value("${app.currency:PEN}")
    private String currency;

    @Override
    @Transactional(readOnly = true)
    public CheckoutResumenView construirResumen(Usuario usuario) {
        Map<UUID, CarritoSession.CarritoSessionItem> items = carritoSession.getItems();
        List<MetodoPago> metodos = new ArrayList<>(EnumSet.allOf(MetodoPago.class));
        List<MetodoPagoGuardadoView> guardados = metodoPagoGuardadoService.listarGuardados(usuario.getId());

        if (items.isEmpty()) {
            Integer saldo = billeteraRepository.findByUsuarioId(usuario.getId())
                    .map(b -> walletTxRepository.calcularSaldoCentavos(b.getId()))
                    .orElse(null);
            if (saldo == null) {
                metodos.remove(MetodoPago.BILLETERA);
            }
            return new CheckoutResumenView(List.of(), 0, 0, 0, currencySymbol(), metodos, saldo, guardados);
        }

        Map<UUID, Evento> eventos = eventoRepository.findAllById(
                        items.values().stream()
                                .map(CarritoSession.CarritoSessionItem::eventoId)
                                .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Evento::getId, e -> e));

        List<CheckoutItemView> detalle = new ArrayList<>();
        int subtotal = 0;

        for (CarritoSession.CarritoSessionItem item : items.values()) {
            Evento evento = eventos.get(item.eventoId());
            OffsetDateTime fecha = evento != null ? evento.getInicioEn() : null;
            String lugar = evento != null ? formatearLugar(evento) : "Por definir";

            int subtotalItem = item.precioCentavos() * item.cantidad();
            subtotal += subtotalItem;

            detalle.add(new CheckoutItemView(
                    item.entradaId(),
                    item.eventoId(),
                    evento != null ? evento.getTitulo() : item.titulo(),
                    lugar,
                    fecha,
                    item.tipoNombre(),
                    item.cantidad(),
                    item.precioCentavos(),
                    subtotalItem
            ));
        }

        int igv = calcularIgv(subtotal);
        int total = subtotal + igv;

        Integer saldoBilletera = billeteraRepository.findByUsuarioId(usuario.getId())
                .map(b -> walletTxRepository.calcularSaldoCentavos(b.getId()))
                .orElse(null);

        if (saldoBilletera == null) {
            metodos.remove(MetodoPago.BILLETERA);
        }

        return new CheckoutResumenView(detalle, subtotal, igv, total, currencySymbol(), metodos, saldoBilletera, guardados);
    }

    @Override
    public BoletaView procesarPago(Usuario usuario, CheckoutPagoRequest request) {
        Map<UUID, CarritoSession.CarritoSessionItem> items = carritoSession.getItems();
        if (items.isEmpty()) {
            throw new IllegalStateException("Tu carrito esta vacio.");
        }

        MetodoPagoGuardado metodoGuardado = validarMedioPago(usuario, request);

        CheckoutResumenView resumen = construirResumen(usuario);
        if (resumen.items().isEmpty()) {
            throw new IllegalStateException("No hay entradas disponibles para pagar.");
        }

        Map<UUID, List<CarritoSession.CarritoSessionItem>> itemsPorEvento = new LinkedHashMap<>();
        for (CarritoSession.CarritoSessionItem item : items.values()) {
            itemsPorEvento.computeIfAbsent(item.eventoId(), key -> new ArrayList<>()).add(item);
        }

        Map<UUID, Evento> eventos = eventoRepository.findAllById(itemsPorEvento.keySet()).stream()
                .collect(Collectors.toMap(Evento::getId, e -> e));

        String referenciaPago = generarReferenciaPago(request, metodoGuardado);
        MetodoPago metodo = request.getMetodo();

        if (metodo == MetodoPago.BILLETERA) {
            procesarPagoBilletera(usuario, resumen.totalCentavos());
        }

        List<BoletaItemView> boletaItems = new ArrayList<>();
        for (Map.Entry<UUID, List<CarritoSession.CarritoSessionItem>> entry : itemsPorEvento.entrySet()) {
            UUID eventoId = entry.getKey();
            Evento evento = eventos.get(eventoId);
            if (evento == null) {
                throw new IllegalArgumentException("El evento ya no está disponible.");
            }

            Orden orden = new Orden();
            orden.setComprador(usuario);
            orden.setEvento(evento);
            orden.setEstado(EstadoOrden.PAGADA);
            orden.setMetodoPago(metodo);
            orden.setReferenciaPago(referenciaPago);

            int subtotalOrden = 0;

            for (CarritoSession.CarritoSessionItem carritoItem : entry.getValue()) {
                EventoEntradaTipo tipo = eventoEntradaTipoRepository.findById(carritoItem.entradaId())
                        .orElseThrow(() -> new IllegalArgumentException("La entrada ya no está disponible."));

                OrdenItem item = new OrdenItem();
                item.setOrden(orden);
                item.setTipoDeEvento(tipo);
                item.setCantidad(carritoItem.cantidad());
                item.setPrecioUnitCentavos(carritoItem.precioCentavos());
                orden.getItems().add(item);

                int subtotalItem = carritoItem.precioCentavos() * carritoItem.cantidad();
                subtotalOrden += subtotalItem;

                boletaItems.add(new BoletaItemView(
                        evento.getId(),
                        evento.getTitulo(),
                        carritoItem.tipoNombre(),
                        carritoItem.cantidad(),
                        carritoItem.precioCentavos(),
                        subtotalItem
                ));
            }

            orden.setSubtotalCentavos(subtotalOrden);
            orden.setDescuentoCentavos(0);
            orden.setTotalCentavos(subtotalOrden + calcularIgv(subtotalOrden));

            ordenRepository.save(orden);
        }

        carritoService.limpiar();
        guardarMetodoSiCorresponde(usuario, request, metodo, metodoGuardado);

        return new BoletaView(
                generarCodigoBoleta(),
                request.getNombreCompleto(),
                request.getCorreoElectronico(),
                metodo,
                descripcionPago(request, metodoGuardado),
                boletaItems,
                resumen.subtotalCentavos(),
                resumen.igvCentavos(),
                resumen.totalCentavos(),
                currencySymbol(),
                OffsetDateTime.now()
        );
    }

    private MetodoPagoGuardado validarMedioPago(Usuario usuario, CheckoutPagoRequest request) {
        MetodoPago metodo = request.getMetodo();
        if (metodo == null) {
            throw new IllegalArgumentException("Debes seleccionar un metodo de pago.");
        }

        MetodoPagoGuardado metodoGuardado = null;
        UUID guardadoId = request.getMetodoGuardadoId();
        if (guardadoId != null) {
            metodoGuardado = metodoPagoGuardadoService.obtenerParaUsuario(guardadoId, usuario.getId())
                    .orElseThrow(() -> new IllegalArgumentException("El metodo de pago guardado no esta disponible."));
            if (metodoGuardado.getTipo() != metodo) {
                throw new IllegalArgumentException("El metodo guardado seleccionado no coincide con el tipo de pago elegido.");
            }
        }

        switch (metodo) {
            case YAPE, PLIN -> {
                if (metodoGuardado == null) {
                    if (!StringUtils.hasText(request.getTelefono())) {
                        throw new IllegalArgumentException("Debes ingresar el numero asociado al pago movil.");
                    }
                    request.setTelefono(request.telefonoSanitizado());
                } else {
                    request.setTelefono(metodoGuardado.getTelefono());
                }
                if (!StringUtils.hasText(request.getCodigoOperacion())) {
                    throw new IllegalArgumentException("Debes ingresar el codigo o referencia de la operacion.");
                }
            }
            case TARJETA -> {
                if (!StringUtils.hasText(request.getTarjetaCvv())) {
                    throw new IllegalArgumentException("Debes ingresar el CVV de la tarjeta.");
                }
                if (metodoGuardado == null) {
                    if (!StringUtils.hasText(request.getTarjetaNumero())
                            || !StringUtils.hasText(request.getTarjetaExpiracion())
                            || !StringUtils.hasText(request.getTarjetaTitular())) {
                        throw new IllegalArgumentException("Debes completar los datos de la tarjeta.");
                    }
                }
            }
            case BILLETERA -> {
                // Validacion adicional en procesarPagoBilletera
            }
        }

        return metodoGuardado;
    }

    private void procesarPagoBilletera(Usuario usuario, int totalCentavos) {
        Billetera billetera = billeteraRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("No cuentas con una billetera activa."));

        int saldo = walletTxRepository.calcularSaldoCentavos(billetera.getId());
        if (saldo < totalCentavos) {
            throw new IllegalArgumentException("Saldo insuficiente en la billetera.");
        }

        WalletTx tx = new WalletTx();
        tx.setBilletera(billetera);
        tx.setTipo("DB");
        tx.setConcepto("COMPRA_ENTRADAS");
        tx.setMontoCentavos(totalCentavos);
        ObjectNode referencia = JsonNodeFactory.instance.objectNode();
        referencia.put("tipo", "COMPRA");
        referencia.put("descripcion", "Pago de entradas");
        tx.setReferencia(referencia);
        walletTxRepository.save(tx);
    }

    private void guardarMetodoSiCorresponde(Usuario usuario,
                                            CheckoutPagoRequest request,
                                            MetodoPago metodo,
                                            MetodoPagoGuardado metodoGuardado) {
        if (!request.isGuardarMetodo() || metodoGuardado != null) {
            return;
        }
        String alias = StringUtils.hasText(request.getAliasMetodo()) ? request.getAliasMetodo().trim() : null;
        switch (metodo) {
            case TARJETA -> metodoPagoGuardadoService.guardarTarjeta(
                    usuario,
                    request.getTarjetaNumero(),
                    request.getTarjetaExpiracion(),
                    request.getTarjetaTitular(),
                    alias
            );
            case YAPE, PLIN -> metodoPagoGuardadoService.guardarWallet(
                    usuario,
                    metodo,
                    request.telefonoSanitizado(),
                    alias
            );
            default -> {
            }
        }
    }

    private int calcularIgv(int subtotalCentavos) {
        return BigDecimal.valueOf(subtotalCentavos)
                .multiply(IGV_PORCENTAJE)
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    private String descripcionPago(CheckoutPagoRequest request, MetodoPagoGuardado guardado) {
        return switch (request.getMetodo()) {
            case YAPE -> descripcionWallet("Yape", request, guardado);
            case PLIN -> descripcionWallet("Plin", request, guardado);
            case TARJETA -> {
                if (guardado != null) {
                    String marca = StringUtils.hasText(guardado.getMarca()) ? guardado.getMarca() : "Tarjeta";
                    String mascara = StringUtils.hasText(guardado.getMascara()) ? guardado.getMascara() : "****";
                    yield marca + " " + mascara;
                }
                yield "Tarjeta ****" + ultimosDigitos(request.getTarjetaNumero());
            }
            case BILLETERA -> "Billetera digital";
        };
    }

    private String descripcionWallet(String etiqueta, CheckoutPagoRequest request, MetodoPagoGuardado guardado) {
        String alias = null;
        if (guardado != null) {
            if (StringUtils.hasText(guardado.getAlias())) {
                alias = guardado.getAlias();
            } else if (StringUtils.hasText(guardado.getMascara())) {
                alias = guardado.getMascara();
            } else if (StringUtils.hasText(guardado.getIdentificador())) {
                alias = guardado.getIdentificador();
            }
        }
        if (!StringUtils.hasText(alias)) {
            alias = request.telefonoSanitizado();
        }
        String operacion = request.codigoOperacionSanitizado();
        if (StringUtils.hasText(alias)) {
            return etiqueta + " - Operacion " + operacion + " (" + alias + ")";
        }
        return etiqueta + " - Operacion " + operacion;
    }

    private String generarReferenciaPago(CheckoutPagoRequest request, MetodoPagoGuardado guardado) {
        return switch (request.getMetodo()) {
            case YAPE, PLIN -> request.codigoOperacionSanitizado();
            case TARJETA -> guardado != null && StringUtils.hasText(guardado.getIdentificador())
                    ? guardado.getIdentificador()
                    : ultimosDigitos(request.getTarjetaNumero());
            case BILLETERA -> "BILLETERA";
        };
    }

    private String ultimosDigitos(String numero) {
        if (!StringUtils.hasText(numero)) {
            return "0000";
        }
        String limpio = numero.replaceAll("\\D", "");
        if (limpio.length() <= 4) {
            return limpio;
        }
        return limpio.substring(limpio.length() - 4);
    }

    private String formatearLugar(Evento evento) {
        List<String> partes = new ArrayList<>();
        if (StringUtils.hasText(evento.getDistrito())) partes.add(evento.getDistrito());
        if (StringUtils.hasText(evento.getProvincia())) partes.add(evento.getProvincia());
        if (StringUtils.hasText(evento.getDepartamento())) partes.add(evento.getDepartamento());
        if (partes.isEmpty() && StringUtils.hasText(evento.getDireccion())) {
            partes.add(evento.getDireccion());
        }
        return partes.isEmpty() ? "Por definir" : String.join(", ", partes);
    }

    private String currencySymbol() {
        if ("PEN".equalsIgnoreCase(currency) || "S/".equalsIgnoreCase(currency)) {
            return "S/";
        }
        if ("USD".equalsIgnoreCase(currency) || "$".equals(currency)) {
            return "$";
        }
        return currency.toUpperCase(Locale.ROOT);
    }

    private String generarCodigoBoleta() {
        return "BOL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
