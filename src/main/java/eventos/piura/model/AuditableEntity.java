package eventos.piura.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter @Setter
public abstract class AuditableEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid")
  private UUID id;

  @CreationTimestamp
  @Column(name = "creado_en", nullable = false, updatable = false)
  private OffsetDateTime creadoEn;

  @UpdateTimestamp
  @Column(name = "actualizado_en")
  private OffsetDateTime actualizadoEn;
}
