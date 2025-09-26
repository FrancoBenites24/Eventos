package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "tipos_ticket")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 100)
    private String nombre; // Adulto, Niño, Estudiante

    @PositiveOrZero
    private Double precio;

    @Min(0)
    private Integer cantidad;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;
}
