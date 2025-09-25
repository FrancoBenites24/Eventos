package eventos.piura.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import eventos.piura.Seguridad.Model.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
}
