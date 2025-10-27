// src/main/java/eventos/piura/service/RolService.java
package eventos.piura.services;

import eventos.piura.dto.RolForm;
import eventos.piura.model.Rol;

import java.util.List;
import java.util.UUID;

public interface RolService {

  List<Rol> listarConRelaciones();

  Rol obtener(UUID id);

  Rol crear(RolForm form);

  Rol actualizar(RolForm form);

  void eliminar(UUID id);
}
