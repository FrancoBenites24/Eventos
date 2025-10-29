package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name="adm_tipo_entrada", uniqueConstraints = {
  @UniqueConstraint(name="uq_adm_tipo_entrada_nombre", columnNames="nombre")
})
@Getter @Setter @NoArgsConstructor
public class TipoEntradaCatalogo extends AuditableEntity {
  @NotBlank @Size(max=30) @Column(nullable=false, length=30)
  private String nombre;

  @Size(max=200) private String descripcion;
  @Column(nullable=false) private boolean activo = true;
}
