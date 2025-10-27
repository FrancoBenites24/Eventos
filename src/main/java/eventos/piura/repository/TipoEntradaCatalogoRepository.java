package eventos.piura.repository;

import eventos.piura.model.TipoEntradaCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TipoEntradaCatalogoRepository extends JpaRepository<TipoEntradaCatalogo, UUID> {

    List<TipoEntradaCatalogo> findByActivoTrueOrderByNombreAsc();

    Optional<TipoEntradaCatalogo> findByNombreIgnoreCase(String nombre);
}
