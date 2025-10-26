// Suscripcion.java
package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="org_suscripcion")
@Getter @Setter @NoArgsConstructor
@org.hibernate.annotations.Check(constraints = "periodo_fin > periodo_inicio")
public class Suscripcion extends AuditableEntity {
  @ManyToOne(optional=false) @JoinColumn(name="usuario_id") private Usuario usuario;
  @ManyToOne(optional=false) @JoinColumn(name="plan_id")    private Plan plan;

  @NotBlank @Size(max=20) @Column(nullable=false, length=20)
  private String estado; // ACTIVA, IMPAGA, CANCELADA

  @Column(name="periodo_inicio", nullable=false) private OffsetDateTime periodoInicio;
  @Column(name="periodo_fin",    nullable=false) private OffsetDateTime periodoFin;

  @Size(max=120) private String referenciaExterna;
}
