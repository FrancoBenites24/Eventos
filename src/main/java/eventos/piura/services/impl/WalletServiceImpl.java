package eventos.piura.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eventos.piura.dto.PerfilTransaccionView;
import eventos.piura.dto.checkout.MetodoPagoGuardadoView;
import eventos.piura.dto.wallet.WalletRecargaRequest;
import eventos.piura.dto.wallet.WalletResumenView;
import eventos.piura.model.Billetera;
import eventos.piura.model.MetodoPagoGuardado;
import eventos.piura.model.Usuario;
import eventos.piura.model.WalletTx;
import eventos.piura.model.enums.MetodoPago;
import eventos.piura.repository.BilleteraRepository;
import eventos.piura.repository.WalletTxRepository;
import eventos.piura.services.MetodoPagoGuardadoService;
import eventos.piura.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {

    private static final Locale LOCALE_ES_PE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter TX_FORMATTER =
            DateTimeFormatter.ofPattern("d MMM, HH:mm", LOCALE_ES_PE);
    private static final Pattern TARJETA_EXP_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/\\d{2}$");
    private static final Pattern TARJETA_CVV_PATTERN = Pattern.compile("^\\d{3,4}$");

    private final BilleteraRepository billeteraRepository;
    private final WalletTxRepository walletTxRepository;
    private final MetodoPagoGuardadoService metodoPagoGuardadoService;

    @Value("${app.currency:PEN}")
    private String currency;

    @Override
    @Transactional(readOnly = true)
    public WalletResumenView obtenerResumen(Usuario usuario) {
        Billetera billetera = obtenerOCrearBilletera(usuario);
        int saldo = walletTxRepository.calcularSaldoCentavos(billetera.getId());
        List<MetodoPagoGuardadoView> guardados = metodoPagoGuardadoService.listarGuardados(usuario.getId());
        List<PerfilTransaccionView> transacciones = walletTxRepository.findTop5ByBilleteraIdOrderByCreadoEnDesc(billetera.getId())
                .stream()
                .map(this::mapearTransaccion)
                .toList();
        return new WalletResumenView(saldo, currencySymbol(), transacciones, guardados);
    }

    @Override
    public void recargar(Usuario usuario, WalletRecargaRequest request) {
        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debes ingresar un monto válido para recargar.");
        }

        MetodoPago metodo = request.getMetodo();
        if (metodo == null || metodo == MetodoPago.BILLETERA) {
            throw new IllegalArgumentException("Selecciona un método de pago válido para recargar.");
        }

        MetodoPagoGuardado guardado = validarMedioPago(usuario, request, metodo);

        int montoCentavos = request.getMonto()
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .intValueExact();

        Billetera billetera = obtenerOCrearBilletera(usuario);

        WalletTx tx = new WalletTx();
        tx.setBilletera(billetera);
        tx.setTipo("CR");
        tx.setConcepto("RECARGA_MANUAL");
        tx.setMontoCentavos(montoCentavos);
        tx.setReferencia(construirReferencia(metodo, request, guardado));
        walletTxRepository.save(tx);

        guardarMetodoSiCorresponde(usuario, request, metodo, guardado);
    }

    private MetodoPagoGuardado validarMedioPago(Usuario usuario,
                                                WalletRecargaRequest request,
                                                MetodoPago metodo) {
        MetodoPagoGuardado guardado = null;
        UUID guardadoId = request.getMetodoGuardadoId();
        if (guardadoId != null) {
            guardado = metodoPagoGuardadoService.obtenerParaUsuario(guardadoId, usuario.getId())
                    .orElseThrow(() -> new IllegalArgumentException("El método de pago guardado no está disponible."));
            if (guardado.getTipo() != metodo) {
                throw new IllegalArgumentException("El método guardado seleccionado no coincide con el tipo de pago.");
            }
        }

        switch (metodo) {
            case YAPE, PLIN -> {
                if (guardado == null) {
                    if (!StringUtils.hasText(request.getTelefono())) {
                        throw new IllegalArgumentException("Debes ingresar el número asociado al pago móvil.");
                    }
                    request.setTelefono(request.telefonoSanitizado());
                } else {
                    request.setTelefono(guardado.getTelefono());
                }
                if (!StringUtils.hasText(request.getCodigoOperacion())) {
                    throw new IllegalArgumentException("Debes ingresar el código o referencia de la operación.");
                }
            }
            case TARJETA -> {
                if (guardado == null) {
                    String numero = request.tarjetaNumeroSanitizado();
                    String expiracion = request.tarjetaExpiracionSanitizada();
                    String titular = request.tarjetaTitularSanitizado();
                    String cvv = request.tarjetaCvvSanitizado();
                    if (!StringUtils.hasText(numero)
                            || !StringUtils.hasText(expiracion)
                            || !StringUtils.hasText(titular)
                            || !StringUtils.hasText(cvv)) {
                        throw new IllegalArgumentException("Debes completar los datos de la tarjeta.");
                    }
                    if (!TARJETA_EXP_PATTERN.matcher(expiracion).matches()) {
                        throw new IllegalArgumentException("La expiración de la tarjeta no es válida.");
                    }
                    if (!TARJETA_CVV_PATTERN.matcher(cvv).matches()) {
                        throw new IllegalArgumentException("El CVV de la tarjeta no es válido.");
                    }
                    request.setTarjetaNumero(numero);
                    request.setTarjetaExpiracion(expiracion);
                    request.setTarjetaTitular(titular);
                    request.setTarjetaCvv(cvv);
                }
            }
            case BILLETERA -> {
                // No se utiliza para recargas.
            }
        }
        return guardado;
    }

    private void guardarMetodoSiCorresponde(Usuario usuario,
                                            WalletRecargaRequest request,
                                            MetodoPago metodo,
                                            MetodoPagoGuardado guardado) {
        if (!request.isGuardarMetodo() || guardado != null) {
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

    private Billetera obtenerOCrearBilletera(Usuario usuario) {
        return billeteraRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    Billetera billetera = new Billetera();
                    billetera.setUsuario(usuario);
                    return billeteraRepository.save(billetera);
                });
    }

    private PerfilTransaccionView mapearTransaccion(WalletTx tx) {
        String tipo = "CR".equalsIgnoreCase(tx.getTipo()) ? "Abono" : "Débito";
        String fecha = formatearFecha(tx.getCreadoEn());
        String monto = formatearMonto(tx.getTipo(), tx.getMontoCentavos());
        String concepto = StringUtils.hasText(tx.getConcepto()) ? tx.getConcepto().replace('_', ' ') : "Movimiento";
        return new PerfilTransaccionView(
                tx.getId(),
                tipo,
                capitalizar(concepto),
                fecha,
                monto
        );
    }

    private JsonNode construirReferencia(MetodoPago metodo,
                                         WalletRecargaRequest request,
                                         MetodoPagoGuardado guardado) {
        String codigo = request.codigoOperacionSanitizado();
        String telefono = request.telefonoSanitizado();
        if (guardado != null) {
            telefono = guardado.getTelefono();
        }
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("metodo", metodo.name());
        return switch (metodo) {
            case YAPE, PLIN -> {
                node.put("telefono", telefono != null ? telefono : "");
                node.put("codigo", codigo != null ? codigo : "");
                yield node;
            }
            case TARJETA -> {
                String identificador = guardado != null && StringUtils.hasText(guardado.getIdentificador())
                        ? guardado.getIdentificador()
                        : ultimosDigitos(request.getTarjetaNumero());
                node.put("ultimos4", identificador);
                yield node;
            }
            case BILLETERA -> node;
            default -> throw new IllegalArgumentException("Metodo de pago no soportado: " + metodo);
        };
    }

    private String formatearFecha(OffsetDateTime fecha) {
        if (fecha == null) {
            return "-";
        }
        return TX_FORMATTER.format(fecha);
    }

    private String formatearMonto(String tipo, int montoCentavos) {
        BigDecimal monto = BigDecimal.valueOf(montoCentavos).movePointLeft(2);
        String simbolo = currencySymbol();
        String texto = simbolo + " " + monto.setScale(2, RoundingMode.HALF_UP);
        if ("CR".equalsIgnoreCase(tipo)) {
            return "+" + texto;
        }
        return "-" + texto;
    }

    private String capitalizar(String texto) {
        if (!StringUtils.hasText(texto)) {
            return "";
        }
        String lower = texto.toLowerCase(LOCALE_ES_PE);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
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
}

