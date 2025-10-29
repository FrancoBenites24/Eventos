package eventos.piura.controller.admin;

import eventos.piura.model.Plan;
import eventos.piura.service.PlanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/planes")
public class PlanAdminController {

    private final PlanService planService;

    public PlanAdminController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("planes", planService.listar());
        return "admin/planes"; // templates/admin/planes.html
    }

    @PostMapping({"", "/"})
    public String crearOActualizar(
            @RequestParam(required = false) UUID id,
            @RequestParam String codigo,
            @RequestParam String nombre,
            @RequestParam("precioMensual") Double precioMensual, // S/ en UI
            @RequestParam("caracteristicasJson") String caracteristicasJson,
            @RequestParam(value = "activo", defaultValue = "false") boolean activo,
            RedirectAttributes ra
    ) {
        // Validación rápida del JSON (si no es válido, mostrará error amigable)
        try { new com.fasterxml.jackson.databind.ObjectMapper().readTree(caracteristicasJson); }
        catch (Exception e) {
            ra.addFlashAttribute("toastError", "Características no es un JSON válido.");
            return "redirect:/admin/planes";
        }

        Plan plan = (id != null) ? planService.obtener(id) : new Plan();
        if (plan == null) {
            ra.addFlashAttribute("toastError", "El plan no existe.");
            return "redirect:/admin/planes";
        }

        plan.setCodigo(codigo.trim());
        plan.setNombre(nombre.trim());
        // convertir S/ a centavos (Integer, redondeo seguro)
        int centavos = (int) Math.round(precioMensual * 100.0);
        plan.setMontoMensualCentavos(centavos);
        plan.setCaracteristicas(caracteristicasJson.trim());
        plan.setActivo(activo);

        planService.guardar(plan);
        ra.addFlashAttribute("toastSuccess", "Plan guardado correctamente.");
        return "redirect:/admin/planes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable UUID id, RedirectAttributes ra) {
        // TODO: regla no-borrar-si-tiene-suscripciones
        planService.eliminar(id);
        ra.addFlashAttribute("toastSuccess", "Plan eliminado.");
        return "redirect:/admin/planes";
    }
}
