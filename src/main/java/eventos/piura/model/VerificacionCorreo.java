package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="seg_verificacion_correo",
  uniqueConstraints = @UniqueConstraint(name="uq_seg_verif_unica", columnNames={"usuario_id","codigo"})
)
@Getter @Setter @NoArgsConstructor
@org.hibernate.annotations.Check(constraints =
  "(codigo ~ '^[A-Za-z0-9]{6,8}$')"
)
public class VerificacionCorreo extends AuditableEntity {

  @ManyToOne(optional=false) @JoinColumn(name="usuario_id")
  private Usuario usuario;

  @NotBlank @Size(min=6, max=8) @Column(nullable=false, length=8)
  private String codigo;

  @Column(name="vence_en", nullable=false)
  private OffsetDateTime venceEn;

  private OffsetDateTime consumidoEn;
}
