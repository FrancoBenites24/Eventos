package eventos.piura.service.impl;

import eventos.piura.model.Plan;
import eventos.piura.repository.PlanRepository;
import eventos.piura.service.PlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PlanServiceImpl implements PlanService {

    private final PlanRepository repo;

    public PlanServiceImpl(PlanRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plan> listar() {
        return repo.findAllByOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Plan obtener(UUID id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public Plan guardar(Plan plan) {
        return repo.save(plan);
    }

    @Override
    public void eliminar(UUID id) {
        repo.deleteById(id);
    }
}
