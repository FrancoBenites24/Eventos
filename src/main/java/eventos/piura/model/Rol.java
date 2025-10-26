package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="seg_rol", uniqueConstraints = {
  @UniqueConstraint(name="uq_seg_rol_nombre", columnNames="nombre")
})
@Getter @Setter @NoArgsConstructor
public class Rol extends AuditableEntity {

  @NotBlank @Size(max=20) @Column(nullable=false, length=30)
  private String nombre;

  @ManyToMany
  @JoinTable(name="seg_rol_permiso",
    joinColumns = @JoinColumn(name="rol_id"),
    inverseJoinColumns = @JoinColumn(name="permiso_id"))
  private Set<Permiso> permisos = new HashSet<>();
}
