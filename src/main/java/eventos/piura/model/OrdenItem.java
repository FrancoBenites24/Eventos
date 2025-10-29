package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name="ven_orden_item")
@Getter @Setter @NoArgsConstructor
public class OrdenItem extends AuditableEntity {
  @ManyToOne(optional=false) @JoinColumn(name="orden_id")
  private Orden orden;

  @ManyToOne(optional=false) @JoinColumn(name="evento_entrada_tipo_id")
  private EventoEntradaTipo tipoDeEvento;

  @Positive @Column(nullable=false) private Integer cantidad;
  @PositiveOrZero @Column(name="precio_unit_centavos", nullable=false)
  private Integer precioUnitCentavos;
}
