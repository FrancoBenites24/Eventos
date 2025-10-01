package eventos.piura.config;

import eventos.piura.model.User;
import eventos.piura.model.UserProfile;
import eventos.piura.services.UserProfileService;
import eventos.piura.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    @Transactional
    public CommandLineRunner initData(UserService userService, UserProfileService profileService) {
        return args -> {
            logger.info("Inicializando datos...");
            
            userService.findAll().forEach(user -> {
                if (user.getProfile() == null) {
                    logger.info("Creando perfil para usuario: {}", user.getUsername());
                    UserProfile profile = profileService.createProfileForUser(user);
                    user.setProfile(profile);
                    userService.save(user);
                    logger.info("Perfil creado para usuario: {}", user.getUsername());
                } else {
                    logger.info("El usuario {} ya tiene perfil", user.getUsername());
                }
            });
            
            logger.info("Inicialización completada");
        };
    }
}