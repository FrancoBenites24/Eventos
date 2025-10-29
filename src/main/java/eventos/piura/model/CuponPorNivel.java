package eventos.piura.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="gam_cupon_por_nivel")
@Getter @Setter @NoArgsConstructor
public class CuponPorNivel {
  @EmbeddedId private CuponPorNivelId id = new CuponPorNivelId();

  @ManyToOne @MapsId("cuponId") @JoinColumn(name="cupon_id")
  private Cupon cupon;

  @ManyToOne @MapsId("nivel") @JoinColumn(name="nivel")
  private ReglaNivel reglaNivel;
}

@Embeddable
@Getter @Setter @NoArgsConstructor
class CuponPorNivelId implements java.io.Serializable {
  private Integer nivel;
  @Column(name="cupon_id", columnDefinition="uuid") private java.util.UUID cuponId;
}
