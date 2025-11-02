package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "seg_recuperacion_contrasena", indexes = {
        @Index(name = "idx_seg_recuperacion_token", columnList = "token")
})
@Getter
@Setter
@NoArgsConstructor
@org.hibernate.annotations.Check(constraints = "(char_length(token) >= 32) AND (char_length(token) <= 80)")
public class RecuperacionContrasena extends AuditableEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @NotBlank
    @Size(min = 32, max = 80)
    @Column(nullable = false, unique = true, length = 80)
    private String token;

    @Column(name = "vence_en", nullable = false)
    private OffsetDateTime venceEn;

    private OffsetDateTime consumidoEn;
}
