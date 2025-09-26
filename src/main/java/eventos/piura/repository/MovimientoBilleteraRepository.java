package eventos.piura.repository;

import eventos.piura.model.MovimientoBilletera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoBilleteraRepository extends JpaRepository<MovimientoBilletera, Long> {
    List<MovimientoBilletera> findTop5ByBilletera_UsuarioIdOrderByCreadoEnDesc(Long usuarioId);
}