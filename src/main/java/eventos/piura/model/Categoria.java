package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "ev_categoria", uniqueConstraints = {
    @UniqueConstraint(name = "uq_ev_categoria_nombre", columnNames = "nombre")
})
@Getter
@Setter
@NoArgsConstructor
public class Categoria extends AuditableEntity {
  @NotBlank
  @Size(max = 30)
  @Column(nullable = false, length = 30)
  private String nombre;
}
