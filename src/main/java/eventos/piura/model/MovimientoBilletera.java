package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_billetera")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoBilletera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Double monto;

    @NotBlank
    private String tipo; // recarga, compra, cashback, reembolso

    private String descripcion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "billetera_id", nullable = false)
    private Billetera billetera;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
