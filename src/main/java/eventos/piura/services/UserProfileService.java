package eventos.piura.services;

import eventos.piura.model.User;
import eventos.piura.model.UserProfile;
import eventos.piura.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);
    
    private final UserProfileRepository profileRepository;

    public UserProfileService(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public Optional<UserProfile> findById(Long id) {
        return profileRepository.findById(id);
    }

    public Optional<UserProfile> findByUsername(String username) {
        return profileRepository.findByUserUsername(username);
    }

    public List<UserProfile> findAll() {
        return profileRepository.findAll();
    }

    @Transactional
    public UserProfile save(UserProfile profile) {
        logger.info("Guardando perfil con ID: {}", profile.getId());
        return profileRepository.save(profile);
    }

    public void deleteById(Long id) {
        profileRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return profileRepository.existsById(id);
    }

    public boolean existsByUsername(String username) {
        return profileRepository.existsByUserUsername(username);
    }

    @Transactional
    public UserProfile createProfileForUser(User user) {
        logger.info("Creando perfil para el usuario: {}", user.getUsername());
        
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        
        // Construir el nombre a partir de firstName y lastName
        String name = "";
        if (user.getFirstName() != null && !user.getFirstName().isEmpty()) {
            name = user.getFirstName();
        }
        if (user.getLastName() != null && !user.getLastName().isEmpty()) {
            if (!name.isEmpty()) {
                name += " ";
            }
            name += user.getLastName();
        }
        
        // Si no hay nombre, usar el username
        if (name.isEmpty()) {
            name = user.getUsername();
        }
        
        profile.setName(name);
        profile.setPoints(0);
        profile.setLevel(1);
        profile.setBalance(0.0);
        
        UserProfile savedProfile = profileRepository.save(profile);
        logger.info("Perfil creado con ID: {}", savedProfile.getId());
        
        return savedProfile;
    }
}