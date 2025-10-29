package eventos.piura.repository;

import eventos.piura.model.MetodoPagoGuardado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetodoPagoGuardadoRepository extends JpaRepository<MetodoPagoGuardado, UUID> {

    List<MetodoPagoGuardado> findByUsuarioIdAndActivoTrueOrderByCreadoEnDesc(UUID usuarioId);

    Optional<MetodoPagoGuardado> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    Optional<MetodoPagoGuardado> findByUsuarioIdAndFingerprint(UUID usuarioId, String fingerprint);

    boolean existsByUsuarioIdAndFingerprint(UUID usuarioId, String fingerprint);
}
