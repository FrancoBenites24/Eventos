package eventos.piura.model;

import eventos.piura.model.enums.MetodoPago;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pay_metodo_guardado", uniqueConstraints = {
        @UniqueConstraint(name = "uq_pay_metodo_guardado_fingerprint", columnNames = {"usuario_id", "fingerprint"})
})
@Getter
@Setter
public class MetodoPagoGuardado extends AuditableEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private MetodoPago tipo;

    @Column(name = "alias", length = 80)
    private String alias;

    @Column(name = "mascara", length = 40)
    private String mascara;

    @Column(name = "identificador", length = 40)
    private String identificador;

    @Column(name = "marca", length = 40)
    private String marca;

    @Column(name = "titular", length = 80)
    private String titular;

    @Column(name = "exp_mes")
    private Integer expMes;

    @Column(name = "exp_anio")
    private Integer expAnio;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "fingerprint", nullable = false, length = 64)
    private String fingerprint;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}
