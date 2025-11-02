package eventos.piura.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ven_boleto_entrada", uniqueConstraints = {
        @UniqueConstraint(name = "uq_boleto_codigo", columnNames = {"codigo"})
})
@Getter
@Setter
@NoArgsConstructor
public class BoletoEntrada extends AuditableEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "orden_id")
    private Orden orden;

    @ManyToOne(optional = false)
    @JoinColumn(name = "orden_item_id")
    private OrdenItem ordenItem;

    @NotBlank
    @Size(max = 40)
    @Column(nullable = false, length = 40)
    private String codigo;

    @NotBlank
    @Size(max = 120)
    @Column(name = "nombre_titular", nullable = false, length = 120)
    private String nombreTitular;

    @Email
    @Size(max = 120)
    @Column(name = "correo_destino", length = 120)
    private String correoDestino;

    @NotBlank
    @Size(max = 160)
    @Column(name = "qr_payload", nullable = false, length = 160)
    private String qrPayload;

    @Column(name = "enviado_en")
    private OffsetDateTime enviadoEn;

    @Column(name = "redimido_en")
    private OffsetDateTime redimidoEn;
}
