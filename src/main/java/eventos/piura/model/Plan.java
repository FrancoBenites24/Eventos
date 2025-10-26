package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "org_plan", uniqueConstraints = {
    @UniqueConstraint(name = "uq_org_plan_codigo", columnNames = "codigo")
})
@Getter
@Setter
@NoArgsConstructor
public class Plan extends AuditableEntity {
  @NotBlank
  @Size(max = 40)
  @Column(nullable = false, length = 40)
  private String codigo;

  @NotBlank
  @Size(max = 80)
  @Column(nullable = false, length = 80)
  private String nombre;

  @PositiveOrZero
  @Column(name = "monto_mensual_centavos", nullable = false)
  private Integer montoMensualCentavos;

  @Column(columnDefinition = "jsonb", nullable = false)
  private String caracteristicas = "{}";

  @Column(nullable = false)
  private boolean activo = true;
}
