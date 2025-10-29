package eventos.piura.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import eventos.piura.model.Rol;

import java.util.*;

public interface RolRepository extends JpaRepository<Rol, UUID> {

  Optional<Rol> findByNombreIgnoreCase(String nombre);

  boolean existsByNombreIgnoreCase(String nombre);

  boolean existsByNombreIgnoreCaseAndIdNot(String nombre, UUID id);

  // Para evitar N+1 al listar: traemos permisos y enlaces a usuarios
  @EntityGraph(attributePaths = {"permisos", "usuarioRoles"})
  List<Rol> findAll();

  @EntityGraph(attributePaths = {"permisos"})
  Optional<Rol> findWithPermisosById(UUID id);
}
