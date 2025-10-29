package eventos.piura.controller;

import eventos.piura.dto.UsuarioResumenView;
import eventos.piura.dto.organizador.NuevoEventoForm;
import eventos.piura.dto.organizador.OrganizadorDashboardChart;
import eventos.piura.dto.organizador.OrganizadorDashboardEvento;
import eventos.piura.dto.organizador.OrganizadorDashboardMetric;
import eventos.piura.dto.organizador.OrganizadorEventoCard;
import eventos.piura.mapper.UsuarioViewMapper;
import eventos.piura.model.Categoria;
import eventos.piura.model.Evento;
import eventos.piura.model.PerfilOrganizador;
import eventos.piura.model.TipoEntradaCatalogo;
import eventos.piura.model.Usuario;
import eventos.piura.model.enums.EstadoEntrada;
import eventos.piura.model.enums.EstadoEvento;
import eventos.piura.model.enums.EstadoOrden;
import eventos.piura.repository.EntradaRepository;
import eventos.piura.repository.EventoRepository;
import eventos.piura.repository.OrdenRepository;
import eventos.piura.repository.PerfilOrganizadorRepository;
import eventos.piura.repository.CategoriaRepository;
import eventos.piura.repository.TipoEntradaCatalogoRepository;
import eventos.piura.repository.UsuarioRepository;
import eventos.piura.services.EventoImagenService;
import eventos.piura.services.OrganizadorEventoService;
import eventos.piura.services.OrganizadorConfigService;
import eventos.piura.dto.organizador.OrganizacionDTO;
import eventos.piura.dto.organizador.NotificationDTO;
import eventos.piura.dto.organizador.PaymentMethodDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/organizador")
@RequiredArgsConstructor
public class OrganizadorController {

    private static final Locale LOCALE_ES = new Locale("es", "PE");
    private static final DateTimeFormatter FECHA_EVENTO_LARGA =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", LOCALE_ES);
    private static final DateTimeFormatter FECHA_EVENTO_CORTA =
            DateTimeFormatter.ofPattern("dd MMM yyyy", LOCALE_ES);
    private static final String IMAGEN_POR_DEFECTO = "/img/placeholder-event.png";

    private final UsuarioRepository usuarioRepository;
    private final EventoRepository eventoRepository;
    private final EntradaRepository entradaRepository;
    private final OrdenRepository ordenRepository;
    private final PerfilOrganizadorRepository perfilOrganizadorRepository;
    private final OrganizadorEventoService organizadorEventoService;
    private final CategoriaRepository categoriaRepository;
    private final TipoEntradaCatalogoRepository tipoEntradaCatalogoRepository;
    private final UsuarioViewMapper usuarioViewMapper;
    private final EventoImagenService eventoImagenService;
    private final OrganizadorConfigService organizadorConfigService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        Usuario usuario = obtenerUsuario(authentication);
        UsuarioResumenView usuarioView = usuarioViewMapper.mapear(usuario);
        model.addAttribute("usuario", usuarioView);

        List<Evento> eventos = eventoRepository.findByOrganizadorIdOrderByInicioEnDesc(usuario.getId());
        Map<UUID, Long> entradasVendidas = calcularEntradasVendidas(eventos);
        Map<UUID, Long> ingresosPorEvento = calcularIngresos(eventos);

        long totalEntradasVendidas = entradasVendidas.values().stream().mapToLong(Long::longValue).sum();
        long totalIngresosCentavos = ingresosPorEvento.values().stream().mapToLong(Long::longValue).sum();
        int capacidadTotal = eventos.stream().mapToInt(this::calcularCapacidad).sum();
        long publicados = eventos.stream().filter(e -> e.getEstado() == EstadoEvento.PUBLICADO).count();
        long borradores = eventos.stream().filter(e -> e.getEstado() == EstadoEvento.BORRADOR).count();

        List<OrganizadorDashboardMetric> resumen = List.of(
                new OrganizadorDashboardMetric(
                        "Eventos publicados",
                        formatNumber(publicados),
                        formatResumenTotal(eventos.size()),
                        "bi-calendar-event"
                ),
                new OrganizadorDashboardMetric(
                        "Borradores",
                        formatNumber(borradores),
                        borradores > 0 ? "Pendientes por publicar" : "Sin borradores",
                        "bi-journal-text"
                ),
                new OrganizadorDashboardMetric(
                        "Entradas vendidas",
                        formatNumber(totalEntradasVendidas),
                        capacidadTotal > 0 ? "Capacidad total: " + formatNumber(capacidadTotal) : "Sin cupo definido",
                        "bi-ticket-perforated"
                ),
                new OrganizadorDashboardMetric(
                        "Ingresos cobrados",
                        formatCurrency(totalIngresosCentavos),
                        totalEntradasVendidas > 0 ? "Ordenes pagadas" : "Sin ventas registradas",
                        "bi-currency-dollar"
                )
        );

        OrganizadorDashboardChart chart = construirChart(eventos, entradasVendidas);
        List<OrganizadorDashboardEvento> proximos = construirProximos(eventos, entradasVendidas);

        model.addAttribute("resumen", resumen);
        model.addAttribute("chart", chart);
        model.addAttribute("proximos", proximos);

        return "organizador/dashboard";
    }

    @GetMapping("/eventos")
    public String eventos(Model model,
                          @RequestParam(value = "busqueda", required = false) String busqueda,
                          @RequestParam(value = "estado", required = false) String estado,
                          @RequestParam(value = "tipo", required = false) String tipo,
                          Authentication authentication) {
        Usuario usuario = obtenerUsuario(authentication);
        UsuarioResumenView usuarioView = usuarioViewMapper.mapear(usuario);
        model.addAttribute("usuario", usuarioView);

        List<Evento> eventos = eventoRepository.findByOrganizadorIdOrderByInicioEnDesc(usuario.getId());
        Map<UUID, Long> entradasVendidas = calcularEntradasVendidas(eventos);
        Map<UUID, Long> ingresosPorEvento = calcularIngresos(eventos);

        String filtroBusqueda = safe(busqueda);
        EstadoEvento estadoFiltro = parseEstado(estado);
        String tipoFiltro = safe(tipo);

        List<Evento> filtrados = eventos.stream()
                .filter(evento -> filtrarPorBusqueda(evento, filtroBusqueda))
                .filter(evento -> filtrarPorEstado(evento, estadoFiltro))
                .filter(evento -> filtrarPorTipo(evento, tipoFiltro))
                .toList();

        List<OrganizadorEventoCard> tarjetas = filtrados.stream()
                .map(evento -> construirEventoCard(
                        evento,
                        entradasVendidas.getOrDefault(evento.getId(), 0L),
                        ingresosPorEvento.getOrDefault(evento.getId(), 0L)
                ))
                .toList();

        model.addAttribute("eventos", tarjetas);
        model.addAttribute("filtros", Map.of(
                "busqueda", filtroBusqueda,
                "estado", estadoFiltro != null ? estadoFiltro.name() : "",
                "tipo", tipoFiltro
        ));
        model.addAttribute("estados", construirOpcionesEstado());
        model.addAttribute("tipos", construirOpcionesTipo(eventos));

        return "organizador/eventos";
    }

    @GetMapping("/eventos/nuevo")
    public String nuevoEvento(Model model, Authentication authentication) {
        Usuario usuario = obtenerUsuario(authentication);
        UsuarioResumenView usuarioView = usuarioViewMapper.mapear(usuario);
        model.addAttribute("usuario", usuarioView);

        NuevoEventoForm form = new NuevoEventoForm();
        inicializarHorarios(form);

        List<Categoria> categorias = categoriaRepository.findAllByOrderByNombreAsc();
        List<TipoEntradaCatalogo> tiposEntrada = tipoEntradaCatalogoRepository.findByActivoTrueOrderByNombreAsc();
        prepararEntradaInicial(form, tiposEntrada);

        model.addAttribute("form", form);
        model.addAttribute("categorias", categorias);
        model.addAttribute("tiposEntrada", tiposEntrada);

        return "organizador/evento-form";
    }

    @PostMapping("/eventos")
    public String crearEvento(@Valid @ModelAttribute("form") NuevoEventoForm form,
                              BindingResult bindingResult,
                              Model model,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuario(authentication);
        UsuarioResumenView usuarioView = usuarioViewMapper.mapear(usuario);
        model.addAttribute("usuario", usuarioView);

        List<Categoria> categorias = categoriaRepository.findAllByOrderByNombreAsc();
        List<TipoEntradaCatalogo> tiposEntrada = tipoEntradaCatalogoRepository.findByActivoTrueOrderByNombreAsc();
        model.addAttribute("categorias", categorias);
        model.addAttribute("tiposEntrada", tiposEntrada);

        form.getEntradas().removeIf(this::entradaVacia);
        if (form.getEntradas().isEmpty()) {
            bindingResult.rejectValue("entradas", "entradas.vacias", "Debe registrar al menos un tipo de entrada.");
        }

        if (bindingResult.hasErrors()) {
            asegurarEntradaMinima(form);
            prepararEntradaInicial(form, tiposEntrada);
            return "organizador/evento-form";
        }

        try {
            organizadorEventoService.crearEvento(usuario, form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("error", ex.getMessage());
            asegurarEntradaMinima(form);
            prepararEntradaInicial(form, tiposEntrada);
            return "organizador/evento-form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Evento creado correctamente.");
        return "redirect:/organizador/eventos";
    }

    @GetMapping("/configuracion")
    public String configuracion(Model model,
                                @RequestParam(name = "tab", required = false) String tab,
                                Authentication authentication) {
        Usuario usuario = obtenerUsuario(authentication);
        UsuarioResumenView usuarioView = usuarioViewMapper.mapear(usuario);
        model.addAttribute("usuario", usuarioView);
        model.addAttribute("tabActiva", Optional.ofNullable(tab).orElse("perfil"));

        PerfilOrganizador perfilOrganizador = perfilOrganizadorRepository.findByUsuarioId(usuario.getId())
                .orElse(null);

        Map<String, Object> perfil = new HashMap<>();
        perfil.put("iniciales", usuarioView.iniciales());
        perfil.put("nombre", usuarioView.nombre());
        perfil.put("apellido", usuarioView.apellido());
        perfil.put("correo", usuarioView.correo());
        perfil.put("telefono", safe(usuarioView.telefono()));
        perfil.put("bio", perfilOrganizador != null && perfilOrganizador.getBiografia() != null
                ? perfilOrganizador.getBiografia()
                : "Todavia no agregas una biografia.");
        model.addAttribute("perfil", perfil);

        Map<String, Object> organizacion = new HashMap<>();
        organizacion.put("nombre", perfilOrganizador != null ? safe(perfilOrganizador.getNombrePublico()) : "");
        organizacion.put("web", "");
        organizacion.put("correo", usuarioView.correo());
        organizacion.put("telefono", safe(usuarioView.telefono()));
        organizacion.put("direccion", "");
        organizacion.put("ruc", "");
        model.addAttribute("organizacion", organizacion);

        try {
            List<NotificationDTO> notifs = organizadorConfigService.readNotificaciones(usuario.getId());
            model.addAttribute("notificaciones", notifs);
        } catch (Exception ex) {
            model.addAttribute("notificaciones", Collections.emptyList());
        }
        model.addAttribute("seguridad", Map.of("sesiones", Collections.emptyList()));

        Map<String, Object> facturacion = new HashMap<>();
        facturacion.put("plan", null);
        try {
            List<PaymentMethodDTO> metodos = organizadorConfigService.readMetodosPago(usuario.getId());
            facturacion.put("metodos", metodos);
        } catch (Exception ex) {
            facturacion.put("metodos", Collections.emptyList());
        }
        try {
            List<eventos.piura.dto.organizador.FacturaDTO> facturas = organizadorConfigService.readFacturas(usuario.getId());
            facturacion.put("historial", facturas);
        } catch (Exception ex) {
            facturacion.put("historial", Collections.emptyList());
        }
        model.addAttribute("facturacion", facturacion);

        return "organizador/configuracion";
    }

    @PostMapping(value = "/configuracion/perfil")
    public String guardarPerfil(@RequestParam String nombre,
                                @RequestParam String apellido,
                                @RequestParam String correo,
                                @RequestParam(required = false) String telefono,
                                @RequestParam(required = false) String bio,
                                @RequestParam(required = false) MultipartFile foto,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuario(authentication);
        usuario.setNombre(nombre != null ? nombre.trim() : usuario.getNombre());
        usuario.setApellido(apellido != null ? apellido.trim() : usuario.getApellido());
        usuario.setCorreo(correo != null ? correo.trim().toLowerCase() : usuario.getCorreo());
        usuario.setTelefono(telefono != null ? telefono.trim() : usuario.getTelefono());
        usuarioRepository.save(usuario);

        PerfilOrganizador perfil = perfilOrganizadorRepository.findByUsuarioId(usuario.getId()).orElseGet(() -> {
            PerfilOrganizador p = new PerfilOrganizador();
            p.setUsuario(usuario);
            p.setNombrePublico(usuario.getNombre() + " " + usuario.getApellido());
            return p;
        });
        perfil.setBiografia(bio);
        perfilOrganizadorRepository.save(perfil);

        try {
            if (foto != null && !foto.isEmpty()) {
                var opt = organizadorConfigService.guardarImagenPerfil(usuario.getId(), foto.getOriginalFilename(), foto.getBytes());
                if (opt.isPresent()) {
                    OrganizacionDTO org = organizadorConfigService.readOrganizacion(usuario.getId()).orElse(new OrganizacionDTO());
                    // Usamos logoPath del JSON para almacenar la foto de perfil si no hay otro campo
                    org.setLogoPath(opt.get());
                    organizadorConfigService.writeOrganizacion(usuario.getId(), org);
                }
            }

            NotificationDTO n = new NotificationDTO();
            n.setId(java.util.UUID.randomUUID().toString());
            n.setTipo("perfil");
            n.setTitulo("Perfil actualizado");
            n.setMensaje("Tu perfil ha sido actualizado correctamente.");
            n.setDescripcion(n.getMensaje());
            organizadorConfigService.pushNotificacion(usuario.getId(), n);
        } catch (Exception ex) {
            // no bloquear guardado en DB por errores de archivo
        }

        redirectAttributes.addFlashAttribute("successMessage", "Perfil guardado correctamente.");
        return "redirect:/organizador/configuracion?tab=perfil";
    }

    @PostMapping(value = "/configuracion/organizacion")
    public String guardarOrganizacion(@RequestParam String nombre,
                                      @RequestParam(required = false) String ruc,
                                      @RequestParam(required = false) String direccion,
                                      @RequestParam(required = false) String correoOrg,
                                      @RequestParam(required = false) String telefonoOrg,
                                      @RequestParam(required = false) MultipartFile logo,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuario(authentication);
        OrganizacionDTO org = new OrganizacionDTO();
        org.setNombre(nombre != null ? nombre.trim() : "");
        org.setRuc(ruc != null ? ruc.trim() : "");
        org.setDireccion(direccion != null ? direccion.trim() : "");
        org.setCorreo(correoOrg != null ? correoOrg.trim() : usuario.getCorreo());
        org.setTelefono(telefonoOrg != null ? telefonoOrg.trim() : usuario.getTelefono());

        try {
            if (logo != null && !logo.isEmpty()) {
                var opt = organizadorConfigService.guardarImagenPerfil(usuario.getId(), logo.getOriginalFilename(), logo.getBytes());
                opt.ifPresent(org::setLogoPath);
            }
            organizadorConfigService.writeOrganizacion(usuario.getId(), org);

            NotificationDTO n = new NotificationDTO();
            n.setId(java.util.UUID.randomUUID().toString());
            n.setTipo("organizacion");
            n.setTitulo("Organización actualizada");
            n.setMensaje("Los datos de tu organización han sido guardados.");
            n.setDescripcion(n.getMensaje());
            organizadorConfigService.pushNotificacion(usuario.getId(), n);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo guardar la organización (error de archivos).");
            return "redirect:/organizador/configuracion?tab=organizacion";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Organización guardada correctamente.");
        return "redirect:/organizador/configuracion?tab=organizacion";
    }

    @PostMapping(value = "/configuracion/seguridad/password")
    public String cambiarPassword(@RequestParam String actual,
                                  @RequestParam String nueva,
                                  @RequestParam String confirmacion,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuario(authentication);
        if (!passwordEncoder.matches(actual, usuario.getContrasenaHash())) {
            redirectAttributes.addFlashAttribute("errorMessage", "La contraseña actual es incorrecta.");
            return "redirect:/organizador/configuracion?tab=seguridad";
        }
        if (!nueva.equals(confirmacion)) {
            redirectAttributes.addFlashAttribute("errorMessage", "La nueva contraseña y la confirmación no coinciden.");
            return "redirect:/organizador/configuracion?tab=seguridad";
        }
        if (nueva.length() < 8 || !nueva.matches(".*[A-Z].*") || !nueva.matches(".*[^A-Za-z0-9].*")) {
            redirectAttributes.addFlashAttribute("errorMessage", "La contraseña debe tener al menos 8 caracteres, una mayúscula y un símbolo.");
            return "redirect:/organizador/configuracion?tab=seguridad";
        }
        usuario.setContrasenaHash(passwordEncoder.encode(nueva));
        usuarioRepository.save(usuario);
        try {
            NotificationDTO n = new NotificationDTO();
            n.setId(java.util.UUID.randomUUID().toString());
            n.setTipo("seguridad");
            n.setTitulo("Contraseña cambiada");
            n.setMensaje("Tu contraseña ha sido actualizada exitosamente.");
            n.setDescripcion(n.getMensaje());
            organizadorConfigService.pushNotificacion(usuario.getId(), n);
        } catch (Exception ex) {
            // ignore
        }

        redirectAttributes.addFlashAttribute("successMessage", "Contraseña actualizada.");
        return "redirect:/organizador/configuracion?tab=seguridad";
    }

    @PostMapping(value = "/configuracion/facturacion/metodo")
    public String agregarMetodoPago(@RequestParam String tipo,
                                    @RequestParam String descripcion,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuario(authentication);
        PaymentMethodDTO metodo = new PaymentMethodDTO();
        metodo.setTipo(tipo);
        metodo.setDescripcion(descripcion);
        // datos enmascarados: simple placeholder
        metodo.setDatosEnmascarados("guardado_safe");
        try {
            organizadorConfigService.addMetodoPago(usuario.getId(), metodo);
            NotificationDTO n = new NotificationDTO();
            n.setId(java.util.UUID.randomUUID().toString());
            n.setTipo("facturacion");
            n.setTitulo("Método de pago agregado");
            n.setMensaje("Se añadió un nuevo método de pago.");
            n.setDescripcion(n.getMensaje());
            organizadorConfigService.pushNotificacion(usuario.getId(), n);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo guardar el método de pago.");
            return "redirect:/organizador/configuracion?tab=facturacion";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Método de pago agregado.");
        return "redirect:/organizador/configuracion?tab=facturacion";
    }

    @PostMapping(value = "/configuracion/facturacion/factura")
    public String subirFactura(@RequestParam String periodo,
                               @RequestParam String monto,
                               @RequestParam String estado,
                               @RequestParam(required = false) MultipartFile archivo,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuario(authentication);
        eventos.piura.dto.organizador.FacturaDTO factura = new eventos.piura.dto.organizador.FacturaDTO();
        factura.setId(java.util.UUID.randomUUID().toString());
        factura.setPeriodo(periodo);
        factura.setEstado(estado);
        factura.setMonto(monto);
        factura.setCreadoEn(java.time.LocalDate.now());
        try {
            if (archivo != null && !archivo.isEmpty()) {
                var opt = organizadorConfigService.guardarFacturaFile(usuario.getId(), archivo.getOriginalFilename(), archivo.getBytes());
                opt.ifPresent(factura::setArchivoPath);
            }
            organizadorConfigService.addFactura(usuario.getId(), factura);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo subir la factura.");
            return "redirect:/organizador/configuracion?tab=facturacion";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Factura agregada al historial.");
        return "redirect:/organizador/configuracion?tab=facturacion";
    }

    @GetMapping("/facturacion/factura/{filename:.+}")
    public ResponseEntity<byte[]> servirFactura(@PathVariable String filename, Authentication authentication) {
        try {
            Usuario usuario = obtenerUsuario(authentication);
            java.nio.file.Path p = java.nio.file.Paths.get("data", "organizadores").resolve(usuario.getId().toString()).resolve("invoices").resolve(filename);
            if (!java.nio.file.Files.exists(p)) {
                return ResponseEntity.notFound().build();
            }
            String contentType = java.nio.file.Files.probeContentType(p);
            byte[] bytes = java.nio.file.Files.readAllBytes(p);
            return ResponseEntity.ok().contentType(org.springframework.http.MediaType.parseMediaType(contentType != null ? contentType : org.springframework.http.MediaType.APPLICATION_PDF_VALUE)).body(bytes);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/configuracion/notificaciones/json")
    public ResponseEntity<List<NotificationDTO>> notificacionesJson(Authentication authentication) {
        try {
            Usuario usuario = obtenerUsuario(authentication);
            List<NotificationDTO> list = organizadorConfigService.readNotificaciones(usuario.getId());
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/configuracion/notificaciones/{id}/toggle")
    public String toggleNotificacion(@PathVariable String id, Authentication authentication, RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuario(authentication);
        try {
            List<NotificationDTO> list = organizadorConfigService.readNotificaciones(usuario.getId());
            for (NotificationDTO n : list) {
                if (n.getId().equals(id)) {
                    n.setActivo(n.getActivo() == null ? Boolean.FALSE : !n.getActivo());
                    organizadorConfigService.updateNotificacion(usuario.getId(), n);
                    break;
                }
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo actualizar la notificación.");
        }
        return "redirect:/organizador/configuracion?tab=notificaciones";
    }

    @PostMapping("/configuracion/notificaciones/{id}/delete")
    public String deleteNotificacionAction(@PathVariable String id, Authentication authentication, RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuario(authentication);
        try {
            organizadorConfigService.deleteNotificacion(usuario.getId(), id);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo eliminar la notificación.");
        }
        return "redirect:/organizador/configuracion?tab=notificaciones";
    }

    @GetMapping("/imagen/{filename:.+}")
    public ResponseEntity<byte[]> servirImagen(@PathVariable String filename) {
        try {
            java.nio.file.Path p = java.nio.file.Paths.get("data", "imagenes").resolve(filename);
            if (!java.nio.file.Files.exists(p)) {
                return ResponseEntity.notFound().build();
            }
            String contentType = java.nio.file.Files.probeContentType(p);
            byte[] bytes = java.nio.file.Files.readAllBytes(p);
            return ResponseEntity.ok().contentType(org.springframework.http.MediaType.parseMediaType(contentType != null ? contentType : org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE)).body(bytes);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analitica")
    public String analitica() {
        return "redirect:/organizador/dashboard";
    }

    private Usuario obtenerUsuario(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return usuarioRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Map<UUID, Long> calcularEntradasVendidas(List<Evento> eventos) {
        Map<UUID, Long> resultado = new HashMap<>();
        for (Evento evento : eventos) {
            long vendidos = entradaRepository.countByEventoIdAndEstadoNot(evento.getId(), EstadoEntrada.CANCELADA);
            resultado.put(evento.getId(), vendidos);
        }
        return resultado;
    }

    private Map<UUID, Long> calcularIngresos(List<Evento> eventos) {
        Map<UUID, Long> resultado = new HashMap<>();
        for (Evento evento : eventos) {
            long ingresos = ordenRepository.sumTotalCentavosByEventoIdAndEstado(evento.getId(), EstadoOrden.PAGADA);
            resultado.put(evento.getId(), ingresos);
        }
        return resultado;
    }

    private int calcularCapacidad(Evento evento) {
        return evento.getTipos().stream()
                .map(tipo -> tipo.getCupoTotal() != null ? tipo.getCupoTotal() : 0)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private String formatNumber(long valor) {
        NumberFormat formatter = NumberFormat.getIntegerInstance(LOCALE_ES);
        return formatter.format(valor);
    }

    private String formatCurrency(long centavos) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(LOCALE_ES);
        double soles = centavos / 100.0;
        return formatter.format(soles);
    }

    private String formatResumenTotal(int totalEventos) {
        return totalEventos > 0 ? totalEventos + " en total" : "Sin eventos registrados";
    }

    private OrganizadorDashboardChart construirChart(List<Evento> eventos, Map<UUID, Long> entradasVendidas) {
        Map<YearMonth, MonthAggregate> agregados = new HashMap<>();
        for (Evento evento : eventos) {
            if (evento.getInicioEn() == null) {
                continue;
            }
            YearMonth mes = YearMonth.from(evento.getInicioEn());
            MonthAggregate aggregate = agregados.computeIfAbsent(mes, key -> new MonthAggregate());
            aggregate.eventos++;
            aggregate.asistentes += Math.toIntExact(entradasVendidas.getOrDefault(evento.getId(), 0L));
        }

        List<String> etiquetas = new ArrayList<>();
        List<Integer> datosEventos = new ArrayList<>();
        List<Integer> datosAsistentes = new ArrayList<>();

        YearMonth actual = YearMonth.from(OffsetDateTime.now());
        for (int i = 5; i >= 0; i--) {
            YearMonth mes = actual.minusMonths(i);
            MonthAggregate aggregate = agregados.getOrDefault(mes, MonthAggregate.EMPTY);
            String etiqueta = capitalizar(mes.getMonth().getDisplayName(TextStyle.SHORT, LOCALE_ES));
            etiquetas.add(etiqueta);
            datosEventos.add(aggregate.eventos);
            datosAsistentes.add(aggregate.asistentes);
        }

        return new OrganizadorDashboardChart(etiquetas, datosEventos, datosAsistentes);
    }

    private List<OrganizadorDashboardEvento> construirProximos(List<Evento> eventos,
                                                               Map<UUID, Long> entradasVendidas) {
        OffsetDateTime ahora = OffsetDateTime.now();
        return eventos.stream()
                .filter(evento -> evento.getInicioEn() != null && !evento.getInicioEn().isBefore(ahora))
                .filter(evento -> evento.getEstado() != EstadoEvento.CANCELADO)
                .sorted(Comparator.comparing(Evento::getInicioEn))
                .limit(5)
                .map(evento -> {
                    long vendidos = entradasVendidas.getOrDefault(evento.getId(), 0L);
                    int capacidad = calcularCapacidad(evento);
                    String asistentes = vendidos > 0
                            ? formatNumber(vendidos) + " asistentes confirmados"
                            : (capacidad > 0 ? "Capacidad: " + formatNumber(capacidad) : "Sin cupo definido");
                    return new OrganizadorDashboardEvento(
                            traducirEstadoEvento(evento.getEstado()),
                            safeCategoria(evento),
                            safeTitulo(evento),
                            formatearFechaCorta(evento.getInicioEn()),
                            resolverLugar(evento),
                            asistentes
                    );
                })
                .toList();
    }

    private void inicializarHorarios(NuevoEventoForm form) {
        LocalDate hoy = Optional.ofNullable(form.getInicioFecha()).orElse(LocalDate.now());
        form.setInicioFecha(hoy);
        form.setFinFecha(Optional.ofNullable(form.getFinFecha()).orElse(hoy));

        LocalTime ahora = LocalTime.now().withSecond(0).withNano(0);
        form.setInicioHora(Optional.ofNullable(form.getInicioHora()).orElse(ahora));
        form.setFinHora(Optional.ofNullable(form.getFinHora()).orElse(form.getInicioHora().plusHours(2)));
    }

    private void prepararEntradaInicial(NuevoEventoForm form, List<TipoEntradaCatalogo> tiposEntrada) {
        asegurarEntradaMinima(form);
        if (!tiposEntrada.isEmpty()) {
            TipoEntradaCatalogo defecto = tiposEntrada.get(0);
            NuevoEventoForm.EntradaForm entrada = form.getEntradas().get(0);
            if (entrada.getTipoCatalogoId() == null) {
                entrada.setTipoCatalogoId(defecto.getId());
            }
            if (entrada.getNombreVisible() == null || entrada.getNombreVisible().isBlank()) {
                entrada.setNombreVisible(defecto.getNombre());
            }
        }
        form.getEntradas().forEach(entrada -> {
            if (entrada.getPrecio() == null) {
                entrada.setPrecio(0.0);
            }
            if (entrada.getCupo() == null) {
                entrada.setCupo(100);
            }
        });
    }

    private void asegurarEntradaMinima(NuevoEventoForm form) {
        if (form.getEntradas() == null || form.getEntradas().isEmpty()) {
            form.getEntradas().add(new NuevoEventoForm.EntradaForm());
        }
    }

    private boolean entradaVacia(NuevoEventoForm.EntradaForm entrada) {
        return entrada.getTipoCatalogoId() == null
                && (entrada.getNombreVisible() == null || entrada.getNombreVisible().isBlank())
                && entrada.getPrecio() == null
                && entrada.getCupo() == null;
    }

    private List<Map<String, String>> construirOpcionesEstado() {
        return Arrays.stream(EstadoEvento.values())
                .map(estado -> Map.of(
                        "valor", estado.name(),
                        "label", traducirEstadoEvento(estado)
                ))
                .toList();
    }

    private List<Map<String, String>> construirOpcionesTipo(List<Evento> eventos) {
        Set<String> categorias = new LinkedHashSet<>();
        for (Evento evento : eventos) {
            String categoria = safeCategoria(evento);
            if (!categoria.isBlank()) {
                categorias.add(categoria);
            }
        }
        return categorias.stream()
                .map(valor -> Map.of("valor", valor, "label", valor))
                .toList();
    }

    private OrganizadorEventoCard construirEventoCard(Evento evento, long vendidos, long ingresosCentavos) {
        int capacidad = calcularCapacidad(evento);
        int progreso = capacidad > 0 ? Math.min(100, (int) Math.round((vendidos * 100.0) / capacidad)) : 0;
        String entradas = capacidad > 0
                ? formatNumber(vendidos) + " / " + formatNumber(capacidad) + " entradas vendidas"
                : formatNumber(vendidos) + " entradas vendidas";

        String basePath = "/organizador/eventos/" + evento.getId();
        String imagenUrl = eventoImagenService.obtenerPrimeraImagen(evento.getId())
                .map(eventoImagenService::construirUrl)
                .orElse(IMAGEN_POR_DEFECTO);

        return new OrganizadorEventoCard(
                traducirEstadoEvento(evento.getEstado()),
                safeCategoria(evento),
                safeTitulo(evento),
                formatearFechaLarga(evento.getInicioEn()),
                resolverLugar(evento),
                imagenUrl,
                entradas,
                formatCurrency(ingresosCentavos),
                progreso + "%",
                basePath,
                basePath + "/editar",
                basePath + "/eliminar"
        );
    }

    private boolean filtrarPorBusqueda(Evento evento, String busqueda) {
        if (busqueda.isBlank()) {
            return true;
        }
        String lower = busqueda.toLowerCase(LOCALE_ES);
        return safeTitulo(evento).toLowerCase(LOCALE_ES).contains(lower)
                || safeCategoria(evento).toLowerCase(LOCALE_ES).contains(lower)
                || resolverLugar(evento).toLowerCase(LOCALE_ES).contains(lower);
    }

    private boolean filtrarPorEstado(Evento evento, EstadoEvento estado) {
        return estado == null || evento.getEstado() == estado;
    }

    private boolean filtrarPorTipo(Evento evento, String tipo) {
        if (tipo.isBlank()) {
            return true;
        }
        return safeCategoria(evento).equalsIgnoreCase(tipo);
    }

    private EstadoEvento parseEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        try {
            return EstadoEvento.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String traducirEstadoEvento(EstadoEvento estado) {
        if (estado == null) {
            return "Sin estado";
        }
        return switch (estado) {
            case BORRADOR -> "Borrador";
            case PUBLICADO -> "Publicado";
            case CANCELADO -> "Cancelado";
        };
    }

    private String safeCategoria(Evento evento) {
        if (evento.getCategoria() != null && evento.getCategoria().getNombre() != null
                && !evento.getCategoria().getNombre().isBlank()) {
            return evento.getCategoria().getNombre().trim();
        }
        return "Sin categoria";
    }

    private String safeTitulo(Evento evento) {
        return Optional.ofNullable(evento.getTitulo()).filter(t -> !t.isBlank()).orElse("Evento sin titulo");
    }

    private String formatearFechaLarga(OffsetDateTime fecha) {
        if (fecha == null) {
            return "Fecha por definir";
        }
        return capitalizar(FECHA_EVENTO_LARGA.format(fecha));
    }

    private String formatearFechaCorta(OffsetDateTime fecha) {
        if (fecha == null) {
            return "Fecha por definir";
        }
        return capitalizar(FECHA_EVENTO_CORTA.format(fecha));
    }

    private String resolverLugar(Evento evento) {
        String ubicacion = Stream.of(evento.getDistrito(), evento.getProvincia(), evento.getDepartamento())
                .filter(valor -> valor != null && !valor.isBlank())
                .collect(Collectors.joining(", "));

        if (!ubicacion.isBlank()) {
            return ubicacion;
        }

        if (evento.getDireccion() != null && !evento.getDireccion().isBlank()) {
            return evento.getDireccion();
        }

        return "Ubicacion por definir";
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        String trimmed = texto.trim();
        return trimmed.substring(0, 1).toUpperCase(LOCALE_ES) + trimmed.substring(1);
    }

    private String safe(String valor) {
        return Optional.ofNullable(valor).map(String::trim).orElse("");
    }

    private static final class MonthAggregate {
        private static final MonthAggregate EMPTY = new MonthAggregate();
        private int eventos;
        private int asistentes;
    }
}
