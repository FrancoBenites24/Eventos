package eventos.piura.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
public class CategoriaAdminController {

    @GetMapping({"", "/"})
    public String listar() {
        // templates/admin/Categorias.html (ojo con la mayúscula inicial)
        return "admin/categorias";
    }

    @PostMapping({"", "/"})
    public String crearOActualizar(RedirectAttributes ra) {
        // TODO: servicio crear/actualizar categoría (unicidad por nombre normalizado)
        ra.addFlashAttribute("toastSuccess", "Categoría guardada correctamente.");
        return "redirect:/admin/categorias";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        // TODO: regla no-borrar-si-tiene-eventos
        ra.addFlashAttribute("toastSuccess", "Categoría eliminada.");
        return "redirect:/admin/categorias";
    }
}
