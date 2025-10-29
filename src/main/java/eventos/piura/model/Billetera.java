package eventos.piura.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="wal_billetera",
  uniqueConstraints = @UniqueConstraint(name="uq_wal_billetera_usuario", columnNames="usuario_id")
)
@Getter @Setter @NoArgsConstructor
public class Billetera extends AuditableEntity {
  @OneToOne(optional=false) @JoinColumn(name="usuario_id", unique=true)
  private Usuario usuario;
}
