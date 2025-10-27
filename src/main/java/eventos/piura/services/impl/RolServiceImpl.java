// src/main/java/eventos/piura/service/impl/RolServiceImpl.java
package eventos.piura.services.impl;

import eventos.piura.dto.RolForm;
import eventos.piura.model.Permiso;
import eventos.piura.model.Rol;
import eventos.piura.repository.PermisoRepository;
import eventos.piura.repository.RolRepository;
import eventos.piura.services.RolService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

  private final RolRepository rolRepo;
  private final PermisoRepository permisoRepo;

  @Override
  @Transactional(readOnly = true)
  public List<Rol> listarConRelaciones() {
    // EntityGraph en el repo evita N+1 para permisos y usuarioRoles
    return rolRepo.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Rol obtener(UUID id) {
    return rolRepo.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado."));
  }

  @Override
  public Rol crear(RolForm form) {
    validarNombreUnico(form.getNombre(), null);
    Rol r = new Rol();
    r.setNombre(form.getNombre()); // normaliza en @PrePersist
    r.setPermisos(cargarPermisos(form.getPermisosIds()));
    return rolRepo.save(r);
  }

  @Override
  public Rol actualizar(RolForm form) {
    if (form.getId() == null) throw new IllegalArgumentException("ID requerido.");
    Rol r = obtener(form.getId());
    validarNombreUnico(form.getNombre(), r.getId());
    r.setNombre(form.getNombre()); // normaliza en @PreUpdate
    r.getPermisos().clear();
    r.getPermisos().addAll(cargarPermisos(form.getPermisosIds()));
    return rolRepo.save(r);
  }

  @Override
  public void eliminar(UUID id) {
    Rol r = obtener(id);
    if (!r.getUsuarioRoles().isEmpty()) {
      throw new IllegalStateException("No se puede eliminar: el rol tiene usuarios asignados.");
    }
    rolRepo.delete(r);
  }

  // ========= helpers =========

  private void validarNombreUnico(String nombre, UUID excludeId) {
    if (excludeId == null) {
      if (rolRepo.existsByNombreIgnoreCase(nombre)) {
        throw new IllegalArgumentException("Ya existe un rol con ese nombre.");
      }
    } else {
      if (rolRepo.existsByNombreIgnoreCaseAndIdNot(nombre, excludeId)) {
        throw new IllegalArgumentException("Ya existe un rol con ese nombre.");
      }
    }
  }

  private Set<Permiso> cargarPermisos(Set<UUID> ids) {
    if (ids == null || ids.isEmpty()) return new HashSet<>();
    return new HashSet<>(permisoRepo.findAllById(ids));
  }
}
