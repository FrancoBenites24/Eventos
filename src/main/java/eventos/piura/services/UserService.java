package eventos.piura.services;

import eventos.piura.model.User;
import eventos.piura.model.UserProfile;
import eventos.piura.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileService profileService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserProfileService profileService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileService = profileService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        UserProfile profile = profileService.createProfileForUser(savedUser);
        savedUser.setProfile(profile);
        return savedUser;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public User save(User user) {
        return userRepository.save(user);
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
}