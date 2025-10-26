package eventos.piura.model;

import eventos.piura.model.enums.EstadoAsistencia;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="ev_asistencia",
  uniqueConstraints=@UniqueConstraint(name="uq_ev_asistencia_unica", columnNames={"evento_id","usuario_id"})
)
@Getter @Setter @NoArgsConstructor
public class Asistencia extends AuditableEntity {
  @ManyToOne(optional=false) @JoinColumn(name="evento_id")  private Evento evento;
  @ManyToOne(optional=false) @JoinColumn(name="usuario_id") private Usuario usuario;

  @ManyToOne @JoinColumn(name="entrada_id") private Entrada entrada;

  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
  private EstadoAsistencia estado = EstadoAsistencia.REGISTRADO;

  @Column(name="check_in_en") private java.time.OffsetDateTime checkInEn;

  @Column(length=16, nullable=false) private String origen = "QR"; // QR / MANUAL
}
