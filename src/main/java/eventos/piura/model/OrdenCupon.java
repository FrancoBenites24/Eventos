package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name="ven_orden_cupon", uniqueConstraints =
  @UniqueConstraint(name="uq_ven_orden_cupon", columnNames="orden_id")
)
@Getter @Setter @NoArgsConstructor
public class OrdenCupon extends AuditableEntity {

  @OneToOne(optional=false) @JoinColumn(name="orden_id", unique=true)
  private Orden orden;

  @ManyToOne(optional=false) @JoinColumn(name="cupon_usuario_id")
  private CuponUsuario cuponUsuario;

  @NotBlank @Size(max=24) @Column(name="cupon_codigo", nullable=false, length=24)
  private String cuponCodigo;

  @PositiveOrZero @Column(name="descuento_aplicado_centavos", nullable=false)
  private Integer descuentoAplicadoCentavos;
}
