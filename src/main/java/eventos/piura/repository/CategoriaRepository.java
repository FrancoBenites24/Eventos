package eventos.piura.repository;

import eventos.piura.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
    List<Categoria> findAllByOrderByNombreAsc();
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}
