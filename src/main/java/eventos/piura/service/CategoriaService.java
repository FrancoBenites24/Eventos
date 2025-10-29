package eventos.piura.service;

import eventos.piura.model.Categoria;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CategoriaService {
    List<Categoria> listar();
    Categoria obtener(UUID id);
    Categoria guardar(Categoria categoria);
    void eliminar(UUID id);
    boolean tieneEventosAsociados(UUID id);
    Map<UUID, Long> contarEventosPorCategoria(Collection<UUID> categoriaIds);
}
