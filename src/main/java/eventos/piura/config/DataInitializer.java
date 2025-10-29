package eventos.piura.config;

import eventos.piura.model.Permiso;
import eventos.piura.model.Rol;
import eventos.piura.model.TipoEntradaCatalogo;
import eventos.piura.repository.PermisoRepository;
import eventos.piura.repository.RolRepository;
import eventos.piura.repository.TipoEntradaCatalogoRepository;
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

    @Autowired
    private TipoEntradaCatalogoRepository tipoEntradaCatalogoRepository;

    @Override
    public void run(String... args) {
        // Crear permisos básicos si no existen
        crearPermisosBasicos();

        // Crear roles básicos si no existen
        crearRolesBasicos();

        // Crear catálogos base
        crearTiposEntradaBasicos();
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
        crearPermisoSiNoExiste("VER_ESTADISTICAS", "Permite ver estadisticas de eventos");

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

    private void crearTiposEntradaBasicos() {
        crearTipoEntradaSiNoExiste("General", "Acceso general al evento");
        crearTipoEntradaSiNoExiste("VIP", "Zona VIP con beneficios adicionales");
        crearTipoEntradaSiNoExiste("Preventa", "Entradas en preventa con precio preferencial");
    }

    private void crearPermisoSiNoExiste(String nombre, String descripcion) {
        if (permisoRepository.findByNombre(nombre).isEmpty()) {
            Permiso permiso = new Permiso();
            permiso.setNombre(nombre);
            permiso.setDescripcion(descripcion);
            permisoRepository.save(permiso);
        }
    }

    private void crearTipoEntradaSiNoExiste(String nombre, String descripcion) {
        if (tipoEntradaCatalogoRepository.findByNombreIgnoreCase(nombre).isPresent()) {
            return;
        }
        TipoEntradaCatalogo tipo = new TipoEntradaCatalogo();
        tipo.setNombre(nombre);
        tipo.setDescripcion(descripcion);
        tipo.setActivo(true);
        tipoEntradaCatalogoRepository.save(tipo);
    }
}
