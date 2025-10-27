package eventos.piura.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class UsuarioRolId implements Serializable {
  // NOMBRES alineados a las columnas reales
  private UUID rolId;
  private UUID usuarioId;
}
