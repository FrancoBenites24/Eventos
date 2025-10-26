package eventos.piura.model;

import eventos.piura.model.enums.EstadoUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "seg_usuario", uniqueConstraints = {
    @UniqueConstraint(name = "uq_seg_usuario_username", columnNames = { "username" }),
    @UniqueConstraint(name = "uq_seg_usuario_correo", columnNames = { "correo" }),
    @UniqueConstraint(name = "uq_seg_usuario_dni", columnNames = { "dni" })
})
@Getter
@Setter
@NoArgsConstructor
@org.hibernate.annotations.Check(constraints = "(estado in ('ACTIVO','BLOQUEADO','BANEADO')) and " +
    "(char_length(contrasena_hash) >= 50) and " +
    "(dni ~ '^[0-9]{8}$') and " +
    "(username ~ '^[A-Za-z0-9._-]{3,30}$')")
public class Usuario extends AuditableEntity {

  @NotBlank
  @Size(max = 40)
  private String nombre;
  @NotBlank
  @Size(max = 40)
  private String apellido;

  @NotBlank
  @Pattern(regexp = "^[0-9]{8}$")
  @Column(length = 8, nullable = false)
  private String dni;

  @Pattern(regexp = "^\\+?[0-9]{7,15}$")
  @Column(length = 15)
  private String telefono;

  @NotBlank
  @Size(min = 3, max = 30)
  @Pattern(regexp = "^[A-Za-z0-9._-]{3,30}$")
  @Column(length = 30, nullable = false)
  private String username;

  @NotBlank
  @Email
  @Size(max = 254)
  @Column(columnDefinition = "citext", nullable = false)
  private String correo;

  @NotBlank
  @Size(min = 50)
  @Column(name = "contrasena_hash", length = 100, nullable = false)
  private String contrasenaHash;

  @Column(name = "correo_verificado", nullable = false)
  private boolean correoVerificado = false;

  @Enumerated(EnumType.STRING)
  @Column(length = 10, nullable = false)
  private EstadoUsuario estado = EstadoUsuario.ACTIVO;

  @ManyToMany
  @JoinTable(name = "seg_usuario_rol", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
  private Set<Rol> roles = new HashSet<>();

  @ManyToMany
  @JoinTable(name = "seg_usuario_permiso", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "permiso_id"))
  private Set<Permiso> permisosDirectos = new HashSet<>();
}
