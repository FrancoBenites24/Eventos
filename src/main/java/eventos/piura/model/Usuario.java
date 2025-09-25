package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "usuarios",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "telefono")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 4, max = 30)
    @Column(nullable = false, length = 30)
    private String username;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 30)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 40)
    private String apellido;

    @Email(message = "El email no es válido")
    @NotBlank(message = "El email es obligatorio")
    @Size(max = 50)
    @Column(nullable = false, length = 150)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @Pattern(
        regexp = "^[0-9]{9}$",
        message = "El teléfono debe tener 9 dígitos"
    )
    @Column(length = 9)
    private String telefono;

    @Size(max = 255)
    private String fotoPerfil;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
