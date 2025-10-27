package eventos.piura.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seg_usuario_rol")
@Getter @Setter @NoArgsConstructor
public class UsuarioRol {

  @EmbeddedId
  private UsuarioRolId id;

  // Mapea cada parte de la PK compuesta a su relación
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId("rolId")
  @JoinColumn(name = "rol_id", nullable = false)
  private Rol rol;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId("usuarioId")
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;
}
