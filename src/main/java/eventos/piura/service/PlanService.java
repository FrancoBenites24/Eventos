package eventos.piura.service;

import eventos.piura.model.Plan;

import java.util.List;
import java.util.UUID;

public interface PlanService {
    List<Plan> listar();
    Plan obtener(UUID id);
    Plan guardar(Plan plan);
    void eliminar(UUID id);
}
