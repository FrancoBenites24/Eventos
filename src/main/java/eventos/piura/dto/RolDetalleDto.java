package eventos.piura.dto;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class RolDetalleDto {
  private UUID id;
  private String nombre;
  private Set<UUID> permisosIds;
}

