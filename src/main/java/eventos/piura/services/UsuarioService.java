package eventos.piura.services;

import eventos.piura.dto.RegistroUsuarioRequest;
import eventos.piura.model.Usuario;

import java.util.Optional;

public interface UsuarioService {
    Usuario registrar(RegistroUsuarioRequest request);
    Optional<Usuario> login(String username, String rawPassword);
    boolean existePorCorreo(String correo);
    boolean existePorUsername(String username);
    Optional<Usuario> obtenerPorCorreo(String correo);
}
