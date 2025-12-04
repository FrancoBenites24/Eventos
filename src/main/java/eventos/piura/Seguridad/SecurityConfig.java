package eventos.piura.Seguridad;

import eventos.piura.model.Rol;
import eventos.piura.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;

    public SecurityConfig(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> usuarioRepository.findByUsernameWithRoles(username)
                .map(usuario -> User.builder()
                        .username(usuario.getUsername())
                        .password(usuario.getContrasenaHash())
                        .disabled(!usuario.isCorreoVerificado())
                        .roles(usuario.getRoles().stream()
                                .map(Rol::getNombre)
                                .toArray(String[]::new))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/", "/index", "/css/**", "/js/**", "/images/**", "/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/auth/login")
            .loginProcessingUrl("/auth/login")
            .defaultSuccessUrl("/", true)
            .failureUrl("/auth/login?error=true")
            .permitAll()
        )
        .logout(logout -> logout
            .logoutSuccessUrl("/auth/login?logout=true")
            .permitAll()
        );
    return http.build();
}

}
