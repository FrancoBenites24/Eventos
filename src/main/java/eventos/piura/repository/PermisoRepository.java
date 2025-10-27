package eventos.piura.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import eventos.piura.model.Permiso;

import java.util.UUID;

public interface PermisoRepository extends JpaRepository<Permiso, UUID> {
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, UUID id);
}
