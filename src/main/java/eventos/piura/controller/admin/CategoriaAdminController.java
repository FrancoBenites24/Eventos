package eventos.piura.controller.admin;

import eventos.piura.model.Categoria;
import eventos.piura.service.CategoriaService;
import eventos.piura.controller.admin.validation.CategoriaAdminValidator;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/categorias")
public class CategoriaAdminController {

    private final CategoriaService categoriaService;
    private final CategoriaAdminValidator categoriaValidator;

    public CategoriaAdminController(CategoriaService categoriaService,
                                    CategoriaAdminValidator categoriaValidator) {
        this.categoriaService = categoriaService;
        this.categoriaValidator = categoriaValidator;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        List<Categoria> categorias = categoriaService.listar();
        List<UUID> ids = categorias.stream()
                .map(Categoria::getId)
                .collect(Collectors.toList());
        Map<UUID, Long> eventosPorCategoria = categoriaService.contarEventosPorCategoria(ids);

        model.addAttribute("categorias", categorias);
        model.addAttribute("eventosPorCategoria", eventosPorCategoria);
        return "admin/categorias";
    }

    @PostMapping({"", "/"})
    public String crearOActualizar(
            @RequestParam(required = false) UUID id,
            @RequestParam String nombre,
            RedirectAttributes ra
    ) {
        Categoria categoria = (id != null) ? categoriaService.obtener(id) : new Categoria();
        if (categoria == null) {
            ra.addFlashAttribute("toastError", "La categoria que intenta actualizar no existe.");
            return "redirect:/admin/categorias";
        }

        categoria.setNombre(nombre);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(categoria, "categoria");
        categoriaValidator.validate(categoria, bindingResult);

        if (bindingResult.hasErrors()) {
            String mensaje = bindingResult.getAllErrors().get(0).getDefaultMessage();
            ra.addFlashAttribute("toastError", mensaje);
            return "redirect:/admin/categorias";
        }

        try {
            categoriaService.guardar(categoria);
            ra.addFlashAttribute("toastSuccess", "Categoria guardada correctamente.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable UUID id, RedirectAttributes ra) {
        if (categoriaService.tieneEventosAsociados(id)) {
            ra.addFlashAttribute("toastError", "No se puede eliminar la categoria porque tiene eventos asociados.");
            return "redirect:/admin/categorias";
        }

        try {
            categoriaService.eliminar(id);
            ra.addFlashAttribute("toastSuccess", "Categoria eliminada.");
        } catch (EmptyResultDataAccessException ex) {
            ra.addFlashAttribute("toastError", "La categoria ya no existe.");
        }
        return "redirect:/admin/categorias";
    }
}
