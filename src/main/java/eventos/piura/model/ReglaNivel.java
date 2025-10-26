// ReglaNivel.java
package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name="gam_regla_nivel")
@Getter @Setter @NoArgsConstructor
public class ReglaNivel {
  @Id @Min(1) private Integer nivel;
  @PositiveOrZero @Column(name="puntos_min", nullable=false)
  private Integer puntosMin;
}
