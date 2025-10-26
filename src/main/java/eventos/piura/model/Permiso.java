package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "seg_permiso", uniqueConstraints = {
    @UniqueConstraint(name = "uq_seg_permiso_nombre", columnNames = "nombre")
})
@Getter
@Setter
@NoArgsConstructor
public class Permiso extends AuditableEntity {
  @NotBlank
  @Size(max = 50)
  @Column(nullable = false, length = 50)
  private String nombre;

  @Size(max = 255)
  private String descripcion;
}
