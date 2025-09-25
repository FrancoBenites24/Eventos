package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos", uniqueConstraints = {
    @UniqueConstraint(columnNames = "referencia")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive
    private Double montoTotal;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String metodo; // tarjeta, billetera, yape, plin

    @Column(length = 100)
    private String referencia;

    @NotBlank
    private String estado = "pendiente";

    @PositiveOrZero
    private Double comisionPlataforma = 0.0;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
