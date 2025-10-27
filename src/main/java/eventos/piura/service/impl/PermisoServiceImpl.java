package eventos.piura.service.impl;

import eventos.piura.model.Permiso;
import eventos.piura.repository.PermisoRepository;
import eventos.piura.service.PermisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PermisoServiceImpl implements PermisoService {

    private final PermisoRepository permisoRepository;

    @Override
    public List<Permiso> listar() {
        return permisoRepository.findAll();
    }

    @Override
    public Optional<Permiso> obtener(UUID id) {
        return permisoRepository.findById(id);
    }

    @Override
    public Permiso guardar(Permiso permiso) {
        // Normaliza el código de permiso (opcional)
        if (permiso.getNombre() != null)
            permiso.setNombre(permiso.getNombre().trim().toUpperCase());
        return permisoRepository.save(permiso);
    }

    @Override
    public boolean eliminar(UUID id) {
        if (!permisoRepository.existsById(id)) return false;
        // TODO: validar "no borrar si está asignado a roles"
        permisoRepository.deleteById(id);
        return true;
    }
}
