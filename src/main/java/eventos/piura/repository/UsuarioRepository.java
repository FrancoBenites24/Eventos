package eventos.piura.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eventos.piura.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByUsernameIgnoreCase(String username);
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByDni(String dni);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<Usuario> findByUsernameWithRoles(@Param("username") String username);

    //dashboard
    List<Usuario> findTop5ByOrderByCreadoEnDesc();
}
