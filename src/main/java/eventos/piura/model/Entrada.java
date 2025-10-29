package eventos.piura.model;

import eventos.piura.model.enums.EstadoEntrada;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name="ven_entrada",
  uniqueConstraints = @UniqueConstraint(name="uq_ven_entrada_qr", columnNames="codigo_qr")
)
@Getter @Setter @NoArgsConstructor
public class Entrada extends AuditableEntity {
  @ManyToOne(optional=false) @JoinColumn(name="orden_id")  private Orden orden;
  @ManyToOne(optional=false) @JoinColumn(name="evento_id") private Evento evento;
  @ManyToOne(optional=false) @JoinColumn(name="evento_entrada_tipo_id") private EventoEntradaTipo tipo;

  @NotBlank @Size(min=12, max=64) @Column(name="codigo_qr", nullable=false, length=64)
  private String codigoQr;

  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
  private EstadoEntrada estado = EstadoEntrada.EMITIDA;

  private java.time.OffsetDateTime usadaEn;
}
