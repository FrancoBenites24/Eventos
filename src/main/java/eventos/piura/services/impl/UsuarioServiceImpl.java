package eventos.piura.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import eventos.piura.dto.RegistroUsuarioRequest;
import eventos.piura.model.Usuario;
import eventos.piura.repository.RolRepository;
import eventos.piura.repository.UsuarioRepository;
import eventos.piura.services.UsuarioService;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Usuario registrar(RegistroUsuarioRequest request) {
        // Validar contraseñas coinciden
        if (!request.getContrasena().equals(request.getConfirmarContrasena())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        // Verificar unicidad
        if (usuarioRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new IllegalArgumentException("El username ya existe");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(request.getCorreo())) {
            throw new IllegalArgumentException("El correo ya existe");
        }
        if (usuarioRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("El DNI ya existe");
        }

        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setDni(request.getDni());
        usuario.setTelefono(request.getTelefono());
        usuario.setUsername(request.getUsername());
        usuario.setCorreo(request.getCorreo());
        usuario.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
        usuario.setCorreoVerificado(false);

        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> login(String username, String rawPassword) {
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getContrasenaHash()));
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return usuarioRepository.existsByCorreoIgnoreCase(correo);
    }

    @Override
    public boolean existePorUsername(String username) {
        return usuarioRepository.existsByUsernameIgnoreCase(username);
    }
}
