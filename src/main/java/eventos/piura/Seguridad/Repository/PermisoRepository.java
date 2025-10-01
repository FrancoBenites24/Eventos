package eventos.piura.Seguridad.Repository;

import eventos.piura.Seguridad.Model.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {
}
