package eventos.piura.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="cupon_usuario",
  uniqueConstraints=@UniqueConstraint(name="uq_cupon_usuario_unico", columnNames={"usuario_id","cupon_id"})
)
@Getter @Setter @NoArgsConstructor
public class CuponUsuario extends AuditableEntity {
  @ManyToOne(optional=false) @JoinColumn(name="usuario_id") private Usuario usuario;
  @ManyToOne(optional=false) @JoinColumn(name="cupon_id")   private Cupon cupon;
  @Column(name="asignado_en", nullable=false) private OffsetDateTime asignadoEn = OffsetDateTime.now();
  private OffsetDateTime usadoEn;

  @ManyToOne @JoinColumn(name="orden_id")
  private Orden orden;
}
