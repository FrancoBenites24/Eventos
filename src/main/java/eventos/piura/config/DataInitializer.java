package eventos.piura.config;

import eventos.piura.model.Permiso;
import eventos.piura.model.Rol;
import eventos.piura.repository.PermisoRepository;
import eventos.piura.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PermisoRepository permisoRepository;

    @Override
    public void run(String... args) throws Exception {
        // Crear permisos básicos si no existen
        crearPermisosBasicos();

        // Crear roles básicos si no existen
        crearRolesBasicos();
    }

    private void crearPermisosBasicos() {
        // Permisos básicos para usuarios
        crearPermisoSiNoExiste("VER_EVENTOS", "Permite ver eventos");
        crearPermisoSiNoExiste("COMPRAR_ENTRADAS", "Permite comprar entradas");
        crearPermisoSiNoExiste("GESTIONAR_PERFIL", "Permite gestionar el perfil propio");

        // Permisos para organizadores
        crearPermisoSiNoExiste("CREAR_EVENTOS", "Permite crear eventos");
        crearPermisoSiNoExiste("EDITAR_EVENTOS", "Permite editar eventos propios");
        crearPermisoSiNoExiste("ELIMINAR_EVENTOS", "Permite eliminar eventos propios");
        crearPermisoSiNoExiste("VER_ESTADISTICAS", "Permite ver estadísticas de eventos");

        // Permisos administrativos
        crearPermisoSiNoExiste("GESTIONAR_USUARIOS", "Permite gestionar usuarios");
        crearPermisoSiNoExiste("GESTIONAR_EVENTOS", "Permite gestionar todos los eventos");
        crearPermisoSiNoExiste("GESTIONAR_ROLES", "Permite gestionar roles y permisos");
        crearPermisoSiNoExiste("VER_REPORTES", "Permite ver reportes administrativos");
    }

    private void crearRolesBasicos() {
        // Rol USER
        if (rolRepository.findByNombre("USER").isEmpty()) {
            Rol userRole = new Rol();
            userRole.setNombre("USER");
            userRole.getPermisos().addAll(permisoRepository.findAllByNombreIn(
                Arrays.asList("VER_EVENTOS", "COMPRAR_ENTRADAS", "GESTIONAR_PERFIL")
            ));
            rolRepository.save(userRole);
        }

        // Rol ORGANIZADOR
        if (rolRepository.findByNombre("ORGANIZADOR").isEmpty()) {
            Rol organizadorRole = new Rol();
            organizadorRole.setNombre("ORGANIZADOR");
            organizadorRole.getPermisos().addAll(permisoRepository.findAllByNombreIn(
                Arrays.asList("VER_EVENTOS", "COMPRAR_ENTRADAS", "GESTIONAR_PERFIL",
                "CREAR_EVENTOS", "EDITAR_EVENTOS", "ELIMINAR_EVENTOS", "VER_ESTADISTICAS")
            ));
            rolRepository.save(organizadorRole);
        }

        // Rol ADMIN
        if (rolRepository.findByNombre("ADMIN").isEmpty()) {
            Rol adminRole = new Rol();
            adminRole.setNombre("ADMIN");
            adminRole.getPermisos().addAll(permisoRepository.findAll());
            rolRepository.save(adminRole);
        }
    }

    private void crearPermisoSiNoExiste(String nombre, String descripcion) {
        if (permisoRepository.findByNombre(nombre).isEmpty()) {
            Permiso permiso = new Permiso();
            permiso.setNombre(nombre);
            permiso.setDescripcion(descripcion);
            permisoRepository.save(permiso);
        }
    }
}
