package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name="org_perfil",
  uniqueConstraints = @UniqueConstraint(name="uq_org_perfil_usuario", columnNames="usuario_id")
)
@Getter @Setter @NoArgsConstructor
public class PerfilOrganizador extends AuditableEntity {

  @OneToOne(optional=false) @JoinColumn(name="usuario_id", unique=true)
  private Usuario usuario;

  @NotBlank @Size(max=100) @Column(name="nombre_publico", nullable=false, length=100)
  private String nombrePublico;

  @Column(columnDefinition = "TEXT") private String biografia;
  private java.time.OffsetDateTime activoHasta;
}
