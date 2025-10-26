// Evento.java
package eventos.piura.model;

import eventos.piura.model.enums.EstadoEvento;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="ev_evento")
@Getter @Setter @NoArgsConstructor
@org.hibernate.annotations.Check(constraints = "fin_en > inicio_en")
public class Evento extends AuditableEntity {
  @ManyToOne(optional=false) @JoinColumn(name="organizador_id")
  private Usuario organizador;

  @ManyToOne @JoinColumn(name="categoria_id")
  private Categoria categoria;

  @NotBlank @Size(max=160) @Column(nullable=false, length=160)
  private String titulo;

  @Lob private String descripcion;

  @Column(name="inicio_en", nullable=false) private OffsetDateTime inicioEn;
  @Column(name="fin_en",    nullable=false) private OffsetDateTime finEn;

  @Size(max=200) private String direccion;
  @Size(max=80)  private String distrito;
  @Size(max=80)  private String provincia;
  @Size(max=80)  private String departamento;
  @Size(max=80)  private String pais;

  private BigDecimal latitud;   // numeric(9,6)
  private BigDecimal longitud;  // numeric(9,6)

  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
  private EstadoEvento estado = EstadoEvento.BORRADOR;

  @OneToMany(mappedBy="evento", cascade=CascadeType.ALL, orphanRemoval=true)
  private List<EventoEntradaTipo> tipos = new ArrayList<>();
}
