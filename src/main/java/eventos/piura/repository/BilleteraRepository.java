package eventos.piura.repository;

import eventos.piura.model.Billetera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BilleteraRepository extends JpaRepository<Billetera, UUID> {
    Optional<Billetera> findByUsuarioId(UUID usuarioId);
}
