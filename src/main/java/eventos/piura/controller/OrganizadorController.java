package eventos.piura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/organizador")
public class OrganizadorController {

    private Map<String, Object> usuarioDemo() {
        return Map.of(
                "nombre", "Admin",
                "iniciales", "AD"
        );
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("usuario", usuarioDemo());
        model.addAttribute("resumen", List.of(
                Map.of(
                        "titulo", "Total eventos",
                        "valor", "48",
                        "variacion", "12% mas que el mes anterior",
                        "icono", "bi-calendar-event"
                ),
                Map.of(
                        "titulo", "Total asistentes",
                        "valor", "18,924",
                        "variacion", "8% mas que el mes anterior",
                        "icono", "bi-people"
                ),
                Map.of(
                        "titulo", "Ingresos totales",
                        "valor", "S/ 284,500",
                        "variacion", "15% mas que el mes anterior",
                        "icono", "bi-currency-dollar"
                ),
                Map.of(
                        "titulo", "Tasa de crecimiento",
                        "valor", "23.5%",
                        "variacion", "3% mas que el mes anterior",
                        "icono", "bi-graph-up-arrow"
                )
        ));

        model.addAttribute("proximos", List.of(
                Map.of(
                        "estado", "Proximo",
                        "etiqueta", "Festival cultural",
                        "titulo", "Festival del Limon Chulucanas 2025",
                        "fecha", "15 Nov 2025",
                        "lugar", "Plaza de Armas de Chulucanas",
                        "asistentes", "2500 asistentes esperados"
                ),
                Map.of(
                        "estado", "Proximo",
                        "etiqueta", "Gastronomia",
                        "titulo", "Feria Gastronomica Nortena",
                        "fecha", "20 Nov 2025",
                        "lugar", "Parque Kurt Beer",
                        "asistentes", "3200 asistentes registrados"
                )
        ));

        model.addAttribute("chart", Map.of(
                "etiquetas", List.of("Abr", "May", "Jun", "Jul", "Ago", "Sep"),
                "eventos", List.of(28, 24, 30, 26, 34, 32),
                "asistentes", List.of(3200, 2800, 3600, 3300, 4000, 3820)
        ));

        return "organizador/dashboard";
    }

    @GetMapping("/eventos")
    public String eventos(Model model) {
        model.addAttribute("usuario", usuarioDemo());
        model.addAttribute("filtros", Map.of(
                "busqueda", "",
                "estado", "",
                "tipo", ""
        ));

        model.addAttribute("estados", List.of(
                Map.of("valor", "proximo", "label", "Proximos"),
                Map.of("valor", "en-curso", "label", "En curso"),
                Map.of("valor", "finalizado", "label", "Finalizados")
        ));

        model.addAttribute("tipos", List.of(
                Map.of("valor", "cultural", "label", "Festival cultural"),
                Map.of("valor", "gastronomico", "label", "Feria gastronomica"),
                Map.of("valor", "deportivo", "label", "Evento deportivo")
        ));

        model.addAttribute("eventos", List.of(
                Map.ofEntries(
                        Map.entry("estado", "Proximo"),
                        Map.entry("categoria", "Festival cultural"),
                        Map.entry("titulo", "Festival del Limon Chulucanas 2025"),
                        Map.entry("fecha", "15 Nov 2025"),
                        Map.entry("lugar", "Plaza de Armas de Chulucanas"),
                        Map.entry("entradas", "1850/2500 entradas vendidas"),
                        Map.entry("ingresos", "S/ 45,000"),
                        Map.entry("progreso", "74%"),
                        Map.entry("detalleUrl", "#"),
                        Map.entry("editarUrl", "#"),
                        Map.entry("eliminarUrl", "#")
                ),
                Map.ofEntries(
                        Map.entry("estado", "Proximo"),
                        Map.entry("categoria", "Feria gastronomica"),
                        Map.entry("titulo", "Feria Gastronomica Nortena"),
                        Map.entry("fecha", "20 Nov 2025"),
                        Map.entry("lugar", "Parque Kurt Beer"),
                        Map.entry("entradas", "2100/3200 entradas vendidas"),
                        Map.entry("ingresos", "S/ 62,500"),
                        Map.entry("progreso", "66%"),
                        Map.entry("detalleUrl", "#"),
                        Map.entry("editarUrl", "#"),
                        Map.entry("eliminarUrl", "#")
                ),
                Map.ofEntries(
                        Map.entry("estado", "Proximo"),
                        Map.entry("categoria", "Festival tradicional"),
                        Map.entry("titulo", "Festival de la Marinera"),
                        Map.entry("fecha", "25 Nov 2025"),
                        Map.entry("lugar", "Coliseo Miguel Grau"),
                        Map.entry("entradas", "1200/1800 entradas vendidas"),
                        Map.entry("ingresos", "S/ 38,000"),
                        Map.entry("progreso", "67%"),
                        Map.entry("detalleUrl", "#"),
                        Map.entry("editarUrl", "#"),
                        Map.entry("eliminarUrl", "#")
                )
        ));

        return "organizador/eventos";
    }

    @GetMapping("/configuracion")
    public String configuracion(Model model,
                                @RequestParam(name = "tab", required = false) String tab) {
        model.addAttribute("usuario", usuarioDemo());
        model.addAttribute("tabActiva", tab);

        model.addAttribute("perfil", Map.of(
                "iniciales", "AN",
                "nombre", "Admin",
                "apellido", "Usuario",
                "correo", "admin@eventhub.com",
                "telefono", "+51 234 567 890",
                "bio", "Cuentanos sobre ti..."
        ));

        model.addAttribute("organizacion", Map.of(
                "nombre", "EventHub Piura",
                "correo", "contacto@eventhub.pe",
                "telefono", "+51 073 123 456",
                "direccion", "Av. Grau 234, Piura, Peru",
                "web", "www.eventhub.pe",
                "ruc", "20123456789"
        ));

        model.addAttribute("notificaciones", List.of(
                Map.of(
                        "titulo", "Resumen diario de ventas",
                        "descripcion", "Recibe un resumen con las ventas del dia.",
                        "activo", Boolean.TRUE
                ),
                Map.of(
                        "titulo", "Alertas de baja disponibilidad",
                        "descripcion", "Te avisamos cuando queden menos del 10% de entradas.",
                        "activo", Boolean.TRUE
                ),
                Map.of(
                        "titulo", "Actualizaciones de plataforma",
                        "descripcion", "Mantente al dia con las nuevas funciones.",
                        "activo", Boolean.FALSE
                )
        ));

        model.addAttribute("seguridad", Map.of(
                "sesiones", List.of(
                        Map.of(
                                "dispositivo", "Windows - Chrome",
                                "ubicacion", "Lima, Peru - Activo ahora"
                        ),
                        Map.of(
                                "dispositivo", "iPhone - Safari",
                                "ubicacion", "Piura, Peru - Hace 2 horas"
                        )
                )
        ));

        model.addAttribute("facturacion", Map.of(
                "plan", Map.of(
                        "nombre", "Plan profesional",
                        "descripcion", "Hasta 100 eventos por mes",
                        "precio", "$49/mes"
                ),
                "metodos", List.of(
                        Map.of(
                                "alias", "Visa terminada en 4242",
                                "detalle", "Caduca el 12/25"
                        ),
                        Map.of(
                                "alias", "Mastercard terminada en 6018",
                                "detalle", "Caduca el 05/26"
                        )
                ),
                "historial", List.of(
                        Map.of(
                                "periodo", "Octubre 2025",
                                "estado", "Pagado",
                                "monto", "$49.00"
                        ),
                        Map.of(
                                "periodo", "Septiembre 2025",
                                "estado", "Pagado",
                                "monto", "$49.00"
                        )
                )
        ));

        return "organizador/configuracion";
    }

    @GetMapping("/analitica")
    public String analitica() {
        return "redirect:/organizador/dashboard";
    }
}
