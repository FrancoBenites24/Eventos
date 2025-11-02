package eventos.piura.repository;

import eventos.piura.model.BoletoEntrada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BoletoEntradaRepository extends JpaRepository<BoletoEntrada, UUID> {

    List<BoletoEntrada> findByOrdenId(UUID ordenId);
}
