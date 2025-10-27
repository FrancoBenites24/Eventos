// src/main/java/eventos/piura/controller/admin/RolAdminController.java
package eventos.piura.controller.admin;

import eventos.piura.dto.RolForm;
import eventos.piura.model.Permiso;
import eventos.piura.model.Rol;
import eventos.piura.repository.PermisoRepository;
import eventos.piura.service.RolService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RolAdminController {

  private final RolService rolService;          // <- interfaz
  private final PermisoRepository permisoRepo;

  @GetMapping({"", "/"})
  public String listar(Model model, @ModelAttribute("rolForm") RolForm rolForm) {
    List<Rol> roles = rolService.listarConRelaciones();
    List<Permiso> permisos = permisoRepo.findAll();
    model.addAttribute("roles", roles);
    model.addAttribute("permisos", permisos);
    return "admin/roles";
  }

  @PostMapping({"", "/"})
  public String crearOActualizar(@Valid @ModelAttribute("rolForm") RolForm form,
                                 BindingResult br,
                                 RedirectAttributes ra) {
    if (br.hasErrors()) {
      ra.addFlashAttribute("toastDanger", "Revisa el formulario.");
      ra.addFlashAttribute("org.springframework.validation.BindingResult.rolForm", br);
      ra.addFlashAttribute("rolForm", form);
      return "redirect:/admin/roles";
    }
    try {
      if (form.getId() == null) {
        rolService.crear(form);
        ra.addFlashAttribute("toastSuccess", "Rol creado correctamente.");
      } else {
        rolService.actualizar(form);
        ra.addFlashAttribute("toastSuccess", "Rol actualizado correctamente.");
      }
    } catch (IllegalArgumentException e) {
      ra.addFlashAttribute("toastDanger", e.getMessage());
      ra.addFlashAttribute("rolForm", form);
    }
    return "redirect:/admin/roles";
  }

  @PostMapping("/{id}/eliminar")
  public String eliminar(@PathVariable UUID id, RedirectAttributes ra) {
    try {
      rolService.eliminar(id);
      ra.addFlashAttribute("toastSuccess", "Rol eliminado.");
    } catch (EntityNotFoundException | IllegalStateException e) {
      ra.addFlashAttribute("toastDanger", e.getMessage());
    }
    return "redirect:/admin/roles";
  }
}
