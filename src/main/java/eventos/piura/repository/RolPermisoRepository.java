package eventos.piura.repository;

import eventos.piura.model.RolPermiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, Long> {
    List<RolPermiso> findByRolId(Long rolId);
    void deleteByRolId(Long rolId);
}
