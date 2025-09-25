package eventos.piura.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario_cupones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean usado = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cupon_id", nullable = false)
    private Cupon cupon;
}
