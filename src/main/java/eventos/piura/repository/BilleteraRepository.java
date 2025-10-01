package eventos.piura.repository;

import eventos.piura.model.Billetera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BilleteraRepository extends JpaRepository<Billetera, Long> {
    Optional<Billetera> findByUsuarioId(Long usuarioId);
}