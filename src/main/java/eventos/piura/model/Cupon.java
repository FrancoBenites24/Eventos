package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "cupones", uniqueConstraints = {
    @UniqueConstraint(columnNames = "codigo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String codigo;

    @NotBlank
    private String tipoDescuento; // porcentaje, fijo

    @Positive
    private Double valorDescuento;
}
