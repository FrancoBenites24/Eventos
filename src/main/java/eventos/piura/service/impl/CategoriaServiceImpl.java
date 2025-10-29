package eventos.piura.service.impl;

import eventos.piura.model.Categoria;
import eventos.piura.repository.CategoriaRepository;
import eventos.piura.repository.EventoRepository;
import eventos.piura.repository.EventoRepository.EventoCategoriaConteo;
import eventos.piura.service.CategoriaService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final EventoRepository eventoRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, EventoRepository eventoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.eventoRepository = eventoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findAllByOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria obtener(UUID id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        if (categoria.getNombre() != null) {
            categoria.setNombre(categoria.getNombre().trim());
        }
        try {
            return categoriaRepository.save(categoria);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Ya existe una categoria con el mismo nombre.", ex);
        }
    }

    @Override
    public void eliminar(UUID id) {
        categoriaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneEventosAsociados(UUID id) {
        return eventoRepository.existsByCategoriaId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Long> contarEventosPorCategoria(Collection<UUID> categoriaIds) {
        Map<UUID, Long> resultado = new HashMap<>();
        if (categoriaIds == null || categoriaIds.isEmpty()) {
            return resultado;
        }

        List<EventoCategoriaConteo> conteos = eventoRepository.contarEventosPorCategoriaIds(categoriaIds);
        for (EventoCategoriaConteo conteo : conteos) {
            resultado.put(conteo.getCategoriaId(), conteo.getTotal());
        }

        for (UUID id : categoriaIds) {
            resultado.putIfAbsent(id, 0L);
        }

        return resultado;
    }
}
