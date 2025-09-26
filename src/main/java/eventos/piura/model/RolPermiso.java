package eventos.piura.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rol_permisos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolPermiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @ManyToOne(optional = false)
    @JoinColumn(name = "permiso_id", nullable = false)
    private Permiso permiso;
}
