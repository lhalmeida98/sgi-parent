package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.RolRepository;
import ec.sgi.backend.domain.model.Rol;
import ec.sgi.backend.infrastructure.persistence.entity.RolEntity;
import ec.sgi.backend.infrastructure.persistence.entity.RolPermisoEntity;
import ec.sgi.backend.infrastructure.persistence.repository.RolJpaRepository;
import ec.sgi.backend.infrastructure.persistence.repository.RolPermisoJpaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RolRepositoryAdapter implements RolRepository {
  private final RolJpaRepository rolJpaRepository;
  private final RolPermisoJpaRepository rolPermisoJpaRepository;

  public RolRepositoryAdapter(
      RolJpaRepository rolJpaRepository,
      RolPermisoJpaRepository rolPermisoJpaRepository
  ) {
    this.rolJpaRepository = rolJpaRepository;
    this.rolPermisoJpaRepository = rolPermisoJpaRepository;
  }

  @Override
  public Rol save(Rol rol) {
    RolEntity entity = toEntity(rol);
    if (entity.getCreadoEn() == null) {
      entity.setCreadoEn(LocalDateTime.now());
    }
    RolEntity saved = rolJpaRepository.save(entity);
    if (!rol.permisos().isEmpty()) {
      List<RolPermisoEntity> permisos = new ArrayList<>();
      for (String permiso : rol.permisos()) {
        RolPermisoEntity permisoEntity = new RolPermisoEntity();
        permisoEntity.setRolId(saved.getId());
        permisoEntity.setAccion(permiso);
        permisos.add(permisoEntity);
      }
      rolPermisoJpaRepository.saveAll(permisos);
    }
    return new Rol(saved.getId(), saved.getEmpresaId(), saved.getNombre(), saved.getDescripcion(), rol.permisos());
  }

  @Override
  public List<Rol> findByEmpresaId(Long empresaId) {
    List<RolEntity> roles = rolJpaRepository.findByEmpresaId(empresaId);
    if (roles.isEmpty()) {
      return Collections.emptyList();
    }
    List<Long> ids = roles.stream().map(RolEntity::getId).toList();
    Map<Long, List<String>> permisosPorRol = new HashMap<>();
    for (RolPermisoEntity permiso : rolPermisoJpaRepository.findByRolIdIn(ids)) {
      permisosPorRol.computeIfAbsent(permiso.getRolId(), key -> new ArrayList<>())
          .add(permiso.getAccion());
    }
    return roles.stream()
        .map(rol -> new Rol(
            rol.getId(),
            rol.getEmpresaId(),
            rol.getNombre(),
            rol.getDescripcion(),
            permisosPorRol.getOrDefault(rol.getId(), List.of())
        ))
        .toList();
  }

  @Override
  public boolean existsPermiso(Long empresaId, String nombreRol, String permiso) {
    Optional<RolEntity> rol = rolJpaRepository.findByEmpresaIdAndNombre(empresaId, nombreRol);
    if (rol.isEmpty()) {
      return false;
    }
    return rolPermisoJpaRepository.existsByRolIdAndAccion(rol.get().getId(), permiso);
  }

  @Override
  public boolean existsByNombre(Long empresaId, String nombreRol) {
    return rolJpaRepository.existsByEmpresaIdAndNombre(empresaId, nombreRol);
  }

  private RolEntity toEntity(Rol rol) {
    RolEntity entity = new RolEntity();
    entity.setId(rol.id());
    entity.setEmpresaId(rol.empresaId());
    entity.setNombre(rol.nombre());
    entity.setDescripcion(rol.descripcion());
    return entity;
  }
}
