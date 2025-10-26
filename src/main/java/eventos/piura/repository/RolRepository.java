package eventos.piura.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import eventos.piura.model.Rol;

import java.util.Optional;
import java.util.UUID;

public interface RolRepository extends JpaRepository<Rol, UUID> {
    Optional<Rol> findByNombre(String nombre);
}
