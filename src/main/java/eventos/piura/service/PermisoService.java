package eventos.piura.service;

import eventos.piura.model.Permiso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermisoService {
    List<Permiso> listar();
    Optional<Permiso> obtener(UUID id);
    Permiso guardar(Permiso permiso);   // crea o actualiza
    boolean eliminar(UUID id);
}
