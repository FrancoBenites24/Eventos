package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name="ev_evento_entrada_tipo")
@Getter @Setter @NoArgsConstructor
public class EventoEntradaTipo extends AuditableEntity {
  @ManyToOne(optional=false) @JoinColumn(name="evento_id")
  private Evento evento;

  @ManyToOne(optional=false) @JoinColumn(name="tipo_entrada_id")
  private TipoEntradaCatalogo tipoEntrada;

  @NotBlank @Size(max=80) @Column(name="nombre_visible", nullable=false, length=80)
  private String nombreVisible;

  @PositiveOrZero @Column(name="precio_centavos", nullable=false)
  private Integer precioCentavos; // 0 = gratuito

  @PositiveOrZero @Column(name="cupo_total", nullable=false)
  private Integer cupoTotal;
}
