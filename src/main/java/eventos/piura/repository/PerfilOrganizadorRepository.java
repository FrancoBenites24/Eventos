package eventos.piura.repository;

import eventos.piura.model.PerfilOrganizador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PerfilOrganizadorRepository extends JpaRepository<PerfilOrganizador, UUID> {

    Optional<PerfilOrganizador> findByUsuarioId(UUID usuarioId);
}
