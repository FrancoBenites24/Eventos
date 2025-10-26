package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name="adm_cupon", uniqueConstraints = {
  @UniqueConstraint(name="uq_adm_cupon_codigo", columnNames="codigo")
})
@Getter @Setter @NoArgsConstructor
public class Cupon extends AuditableEntity {
  @NotBlank @Size(max=24) @Column(nullable=false, length=24)
  private String codigo;

  @Size(max=200) private String descripcion;

  @DecimalMin("0.00") @DecimalMax("100.00")
  private java.math.BigDecimal descuentoPct;          // nullable

  @Positive @Column(name="descuento_fijo_centavos")
  private Integer descuentoFijoCentavos;              // nullable

  @PositiveOrZero @Column(name="minimo_orden_centavos")
  private Integer minimoOrdenCentavos;

  private java.time.OffsetDateTime validoDesde;
  private java.time.OffsetDateTime validoHasta;

  private Integer maxUsoTotal;
  @Column(nullable=false) private boolean activo = true;
}
