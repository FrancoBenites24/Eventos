package eventos.piura.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name="wal_tx")
@Getter @Setter @NoArgsConstructor
@org.hibernate.annotations.Check(constraints = "(tipo in ('CR','DB')) and (monto_centavos > 0)")
public class WalletTx extends AuditableEntity {
  @ManyToOne(optional=false) @JoinColumn(name="billetera_id")
  private Billetera billetera;

  @Column(length=10, nullable=false) private String tipo; // CR/DB
  @NotBlank @Size(max=40) @Column(nullable=false, length=40)
  private String concepto; // VENTA_ENTRADA, CASHIN, CASHOUT, AJUSTE, COMISION_PLATAFORMA

  @Positive @Column(name="monto_centavos", nullable=false)
  private Integer montoCentavos;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition="jsonb", nullable=false)
  private JsonNode referencia = JsonNodeFactory.instance.objectNode();
}
