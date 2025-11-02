package eventos.piura.services.impl;

import eventos.piura.dto.RegistroUsuarioRequest;
import eventos.piura.model.Rol;
import eventos.piura.model.Usuario;
import eventos.piura.repository.RolRepository;
import eventos.piura.repository.UsuarioRepository;
import eventos.piura.services.UsuarioService;
import eventos.piura.services.VerificacionCorreoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private VerificacionCorreoService verificacionCorreoService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public Usuario registrar(RegistroUsuarioRequest request) {
        // Trim de campos de texto para eliminar espacios en blanco
        request.setNombre(request.getNombre().trim());
        request.setApellido(request.getApellido().trim());
        request.setDni(request.getDni().trim());
        if (request.getTelefono() != null) {
            request.setTelefono(request.getTelefono().trim());
        }
        request.setUsername(request.getUsername().trim());
        request.setCorreo(request.getCorreo().trim().toLowerCase()); // Normalizar correo a minúsculas
        request.setContrasena(request.getContrasena().trim());
        request.setConfirmarContrasena(request.getConfirmarContrasena().trim());

        // Validar contraseñas coinciden
        if (!request.getContrasena().equals(request.getConfirmarContrasena())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        // Verificar unicidad
        if (usuarioRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(request.getCorreo())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }
        if (usuarioRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("El DNI ya está registrado");
        }

        // Validar que el nombre de usuario no contenga solo espacios o caracteres inválidos
        if (request.getUsername().isEmpty() || !request.getUsername().matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("El nombre de usuario contiene caracteres inválidos");
        }

        // Validar formato de correo adicionalmente
        if (!request.getCorreo().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido");
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

        // Asignar rol por defecto (USER)
        Rol rolUsuario = rolRepository.findByNombre("USER")
                .orElseThrow(() -> new IllegalStateException("Rol USER no encontrado en la base de datos"));
        usuario.getRoles().add(rolUsuario);

        Usuario guardado = usuarioRepository.save(usuario);

        verificacionCorreoService.crearSolicitudVerificacion(guardado);

        return guardado;
    }

    @Override
    public Optional<Usuario> login(String username, String rawPassword) {
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .filter(u -> u.isCorreoVerificado())
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

    @Override
    public Optional<Usuario> obtenerPorCorreo(String correo) {
        if (correo == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByCorreoIgnoreCase(correo.trim());
    }
}
