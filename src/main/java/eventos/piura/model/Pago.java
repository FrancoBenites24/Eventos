package eventos.piura.model;

import eventos.piura.model.enums.EstadoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "pag_pago")
@Getter
@Setter
@NoArgsConstructor
public class Pago extends AuditableEntity {
  @ManyToOne(optional = false)
  @JoinColumn(name = "orden_id")
  private Orden orden;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoPago estado = EstadoPago.CREADO;

  @NotBlank
  @Size(max = 40)
  @Column(nullable = false, length = 40)
  private String proveedor; // YAPE, PLIN, TARJETA, BILLETERA

  @Size(max = 120)
  @Column(name = "referencia_externa", length = 120)
  private String referenciaExterna;

  @PositiveOrZero
  @Column(name = "monto_centavos", nullable = false)
  private Integer montoCentavos;

  @NotBlank
  @Size(max = 10)
  @Column(nullable = false, length = 10)
  private String moneda = "PEN";
}
