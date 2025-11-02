package eventos.piura.repository;

import eventos.piura.model.RecuperacionContrasena;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecuperacionContrasenaRepository extends JpaRepository<RecuperacionContrasena, UUID> {

    Optional<RecuperacionContrasena> findByToken(String token);

    Optional<RecuperacionContrasena> findTopByUsuarioIdOrderByCreadoEnDesc(UUID usuarioId);

    List<RecuperacionContrasena> findByUsuarioIdAndConsumidoEnIsNull(UUID usuarioId);

    long deleteByUsuarioIdAndConsumidoEnIsNullAndVenceEnBefore(UUID usuarioId, OffsetDateTime fecha);
}
