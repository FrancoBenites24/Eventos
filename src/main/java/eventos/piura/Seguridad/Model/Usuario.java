package eventos.piura.Seguridad.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "usuarios",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_usuarios_username_ci", columnNames = {"username"}),
        @UniqueConstraint(name = "uq_usuarios_email_ci", columnNames = {"email"}),
        @UniqueConstraint(name = "uq_usuarios_dni", columnNames = {"dni"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    @Size(min = 3, max = 20, message = "El username debe tener entre 3 y 20 caracteres")
    @NotBlank(message = "El username es obligatorio")
    private String username;

    @Column(nullable = false, length = 40)
    @Email(message = "Debe ser un correo válido")
    @Size(min = 5, max = 40, message = "El email debe tener entre 5 y 40 caracteres")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @Column(nullable = false, length = 20)
    @Pattern(regexp = "^[0-9A-Za-z._-]{6,20}$", message = "El DNI debe tener entre 6 y 20 caracteres alfanuméricos o . _ -")
    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La contraseña es obligatoria")
    private String passwordHash;

    @Size(max = 30)
    private String nombres;

    @Size(max = 30)
    private String apellidos;

    @Size(max = 9, message = "El teléfono debe tener máximo 9 dígitos")
    private String telefono;

    @Size(max = 20)
    private String ciudad;

    private String direccion;

    private String foto;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuario_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id"),
        uniqueConstraints = @UniqueConstraint(name = "uq_usuario_rol", columnNames = {"usuario_id", "rol_id"})
    )
    private Set<Rol> roles = new HashSet<>();
}
