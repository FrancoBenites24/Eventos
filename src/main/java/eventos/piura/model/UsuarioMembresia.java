package eventos.piura.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_membresias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioMembresia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaInicio = LocalDateTime.now();
    private LocalDateTime fechaFin;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "membresia_id", nullable = false)
    private Membresia membresia;
}
