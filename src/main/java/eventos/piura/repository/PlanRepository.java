package eventos.piura.repository;

import eventos.piura.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, UUID> {
    List<Plan> findAllByOrderByNombreAsc();
}
