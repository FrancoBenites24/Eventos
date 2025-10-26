// TxPuntos.java
package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name="gam_tx_puntos")
@Getter @Setter @NoArgsConstructor
public class TxPuntos extends AuditableEntity {
  @ManyToOne(optional=false) @JoinColumn(name="usuario_id")
  private Usuario usuario;

  @NotBlank @Size(max=40) @Column(nullable=false, length=40)
  private String fuente; // ASISTIR_EVENTO, RESEÑA, OTRO

  @Column(nullable=false) private Integer puntos; // != 0
  @Column(columnDefinition="jsonb", nullable=false) private String meta = "{}";
}
