package eventos.piura.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eventos.piura.model.Permiso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermisoRepository extends JpaRepository<Permiso, UUID> {
    Optional<Permiso> findByNombre(String nombre);

    @Query("SELECT p FROM Permiso p WHERE p.nombre IN :nombres")
    List<Permiso> findAllByNombreIn(@Param("nombres") List<String> nombres);
}
