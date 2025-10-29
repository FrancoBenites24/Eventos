package eventos.piura.model;

import eventos.piura.model.enums.EstadoOrden;
import eventos.piura.model.enums.MetodoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="ven_orden")
@Getter @Setter @NoArgsConstructor
public class Orden extends AuditableEntity {

  @ManyToOne(optional=false) @JoinColumn(name="comprador_id")
  private Usuario comprador;

  @ManyToOne(optional=false) @JoinColumn(name="evento_id")
  private Evento evento;

  @PositiveOrZero @Column(name="subtotal_centavos", nullable=false)
  private Integer subtotalCentavos;

  @PositiveOrZero @Column(name="descuento_centavos", nullable=false)
  private Integer descuentoCentavos = 0;

  @PositiveOrZero @Column(name="total_centavos", nullable=false)
  private Integer totalCentavos;

  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
  private EstadoOrden estado = EstadoOrden.PENDIENTE;

  @Enumerated(EnumType.STRING) @Column(name="metodo_pago", length=20, nullable=false)
  private MetodoPago metodoPago = MetodoPago.TARJETA;

  @Column(name="referencia_pago", length=100)
  private String referenciaPago;

  @OneToMany(mappedBy="orden", cascade=CascadeType.ALL, orphanRemoval=true)
  private List<OrdenItem> items = new ArrayList<>();

  @OneToOne(mappedBy="orden", cascade=CascadeType.ALL, orphanRemoval=true)
  private OrdenCupon cupon;
}
