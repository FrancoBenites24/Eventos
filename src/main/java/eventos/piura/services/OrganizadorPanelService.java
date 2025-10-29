package eventos.piura.services;

import eventos.piura.dto.organizador.NuevoEventoForm;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

public interface OrganizadorPanelService {

    String mostrarDashboard(Model model, Authentication authentication);

    String listarEventos(String busqueda,
                         String estado,
                         String tipo,
                         Model model,
                         Authentication authentication);

    String prepararNuevoEvento(Model model, Authentication authentication);

    String crearEvento(NuevoEventoForm form,
                       BindingResult bindingResult,
                       Model model,
                       Authentication authentication,
                       RedirectAttributes redirectAttributes);

    String prepararEdicionEvento(UUID eventoId, Model model, Authentication authentication);

    String actualizarEvento(UUID eventoId,
                            NuevoEventoForm form,
                            BindingResult bindingResult,
                            Model model,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes);

    String eliminarEvento(UUID eventoId,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes);

    String mostrarConfiguracion(String tab,
                                Model model,
                                Authentication authentication);

    String redirigirAnalitica();
}
