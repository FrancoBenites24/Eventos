package eventos.piura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name="not_correo_log")
@Getter @Setter @NoArgsConstructor
public class CorreoLog extends AuditableEntity {

  @ManyToOne @JoinColumn(name="usuario_id")
  private Usuario usuario;

  @ManyToOne @JoinColumn(name="orden_id")
  private Orden orden;

  @NotBlank @Size(max=40) @Column(nullable=false, length=40)
  private String tipo; // CONFIRMACION_COMPRA, ENTRADAS_QR, OTRO

  @NotBlank @Email @Size(max=254)
  @Column(name="correo_destino", columnDefinition="citext", nullable=false)
  private String correoDestino;

  @NotBlank @Size(max=200) @Column(nullable=false, length=200)
  private String asunto;

  @NotBlank @Lob @Column(nullable=false)
  private String cuerpo;

  @Column(nullable=false) private boolean exito = true;
  private String errorMsg;
}
