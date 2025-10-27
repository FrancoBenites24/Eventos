package eventos.piura.services;

import eventos.piura.dto.RegistroUsuarioRequest;
import eventos.piura.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioService {
    Usuario registrar(RegistroUsuarioRequest request);
    Optional<Usuario> login(String username, String rawPassword);
    boolean existePorCorreo(String correo);
    boolean existePorUsername(String username);
    
    // NUEVOS métodos para administración:
    List<Usuario> listarTodos();
    Optional<Usuario> obtenerPorId(UUID id);
    void eliminar(UUID id);
    Usuario guardar(Usuario usuario);
}
