package eventos.piura.Seguridad.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permisos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "El nombre del permiso es obligatorio")
    @Size(max = 50, message = "El nombre del permiso no puede exceder 50 caracteres")
    private String nombre;

    private String descripcion;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    @ManyToMany(mappedBy = "permisos", fetch = FetchType.LAZY)
    private Set<Rol> roles = new HashSet<>();
}
