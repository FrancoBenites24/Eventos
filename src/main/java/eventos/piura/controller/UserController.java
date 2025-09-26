package eventos.piura.controller;

import eventos.piura.model.Usuario;
import eventos.piura.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UsuarioRepository UsuarioRepository;

    public UserController(UsuarioRepository UsuarioRepository) {
        this.UsuarioRepository = UsuarioRepository;
    }

    @GetMapping
    public List<Usuario> getAllUsers() {
        return UsuarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public Usuario getUserById(@PathVariable Long id) {
        return UsuarioRepository.findById(id).orElseThrow();
    }
}
