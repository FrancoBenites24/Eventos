package eventos.piura.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class RolForm {
  private UUID id;

  @NotBlank
  @Size(max = 20)
  private String nombre;

  // IDs de permisos seleccionados en el <select multiple>
  private Set<UUID> permisosIds;
}
