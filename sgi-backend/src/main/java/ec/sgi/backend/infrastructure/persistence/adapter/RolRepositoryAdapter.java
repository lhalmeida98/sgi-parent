package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.RolRepository;
import ec.sgi.backend.domain.model.Rol;
import ec.sgi.backend.infrastructure.persistence.entity.RolEntity;
import ec.sgi.backend.infrastructure.persistence.entity.RolPermisoEntity;
import ec.sgi.backend.infrastructure.persistence.entity.AccionEntity;
import ec.sgi.backend.infrastructure.persistence.repository.AccionJpaRepository;
import ec.sgi.backend.infrastructure.persistence.repository.RolJpaRepository;
import ec.sgi.backend.infrastructure.persistence.repository.RolPermisoJpaRepository;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.stereotype.Component;

@Component
public class RolRepositoryAdapter implements RolRepository {
  private final RolJpaRepository rolJpaRepository;
  private final RolPermisoJpaRepository rolPermisoJpaRepository;
  private final AccionJpaRepository accionJpaRepository;

  public RolRepositoryAdapter(
      RolJpaRepository rolJpaRepository,
      RolPermisoJpaRepository rolPermisoJpaRepository,
      AccionJpaRepository accionJpaRepository
  ) {
    this.rolJpaRepository = rolJpaRepository;
    this.rolPermisoJpaRepository = rolPermisoJpaRepository;
    this.accionJpaRepository = accionJpaRepository;
  }

  @Override
  public Rol save(Rol rol) {
    RolEntity entity = toEntity(rol);
    if (entity.getCreadoEn() == null) {
      entity.setCreadoEn(LocalDateTime.now());
    }
    if (entity.getActivo() == null) {
      entity.setActivo(true);
    }
    RolEntity saved = rolJpaRepository.save(entity);
    rolPermisoJpaRepository.deleteByRolId(saved.getId());
    if (!rol.accionesIds().isEmpty()) {
      List<RolPermisoEntity> permisos = new ArrayList<>();
      for (Long accionId : rol.accionesIds()) {
        RolPermisoEntity permisoEntity = new RolPermisoEntity();
        permisoEntity.setRolId(saved.getId());
        permisoEntity.setAccionId(accionId);
        permisos.add(permisoEntity);
      }
      rolPermisoJpaRepository.saveAll(permisos);
    }
    return new Rol(
        saved.getId(),
        saved.getNombre(),
        saved.getDescripcion(),
        saved.getActivo() != null && saved.getActivo(),
        saved.getCreadoEn(),
        saved.getActualizadoEn(),
        rol.accionesIds()
    );
  }

  @Override
  public List<Rol> findAll() {
    List<RolEntity> roles = rolJpaRepository.findAll();
    if (roles.isEmpty()) {
      return Collections.emptyList();
    }
    List<Long> ids = roles.stream().map(RolEntity::getId).toList();
    Map<Long, List<Long>> permisosPorRol = new HashMap<>();
    for (RolPermisoEntity permiso : rolPermisoJpaRepository.findByRolIdIn(ids)) {
      if (permiso.getAccionId() == null) {
        continue;
      }
      permisosPorRol.computeIfAbsent(permiso.getRolId(), key -> new ArrayList<>())
          .add(permiso.getAccionId());
    }
    return roles.stream()
        .map(rol -> new Rol(
            rol.getId(),
            rol.getNombre(),
            rol.getDescripcion(),
            rol.getActivo() != null && rol.getActivo(),
            rol.getCreadoEn(),
            rol.getActualizadoEn(),
            permisosPorRol.getOrDefault(rol.getId(), List.of())
        ))
        .toList();
  }

  @Override
  public Optional<Rol> findById(Long id) {
    return rolJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<Rol> findByNombre(String nombreRol) {
    return rolJpaRepository.findByNombre(nombreRol).map(this::toDomain);
  }

  @Override
  public List<String> findPermisosByRoles(List<String> nombresRoles) {
    if (nombresRoles == null || nombresRoles.isEmpty()) {
      return List.of();
    }
    List<RolEntity> roles = rolJpaRepository.findByNombreIn(nombresRoles);
    if (roles.isEmpty()) {
      return List.of();
    }
    List<Long> rolIds = roles.stream().map(RolEntity::getId).toList();
    Set<Long> accionIds = new LinkedHashSet<>();
    for (RolPermisoEntity permiso : rolPermisoJpaRepository.findByRolIdIn(rolIds)) {
      if (permiso.getAccionId() != null) {
        accionIds.add(permiso.getAccionId());
      }
    }
    if (accionIds.isEmpty()) {
      return List.of();
    }
    Set<String> permisos = new LinkedHashSet<>();
    for (AccionEntity accion : accionJpaRepository.findAllById(accionIds)) {
      String codigo = accion.getCodigo();
      if (codigo != null && !codigo.isBlank()) {
        permisos.add(codigo);
      }
    }
    return new ArrayList<>(permisos);
  }

  @Override
  public boolean existsPermiso(String nombreRol, String permiso) {
    Optional<RolEntity> rol = rolJpaRepository.findByNombre(nombreRol);
    if (rol.isEmpty()) {
      return false;
    }
    if (rol.get().getActivo() != null && !rol.get().getActivo()) {
      return false;
    }
    Optional<AccionEntity> accion = accionJpaRepository.findByCodigo(permiso);
    if (accion.isEmpty()) {
      return false;
    }
    return rolPermisoJpaRepository.existsByRolIdAndAccionId(rol.get().getId(), accion.get().getId());
  }

  @Override
  public boolean existsByNombre(String nombreRol) {
    return rolJpaRepository.existsByNombre(nombreRol);
  }

  @Override
  public void deleteById(Long id) {
    rolPermisoJpaRepository.deleteByRolId(id);
    rolJpaRepository.deleteById(id);
  }

  private RolEntity toEntity(Rol rol) {
    RolEntity entity = new RolEntity();
    entity.setId(rol.id());
    entity.setNombre(rol.nombre());
    entity.setDescripcion(rol.descripcion());
    entity.setActivo(rol.activo());
    entity.setCreadoEn(rol.creadoEn());
    entity.setActualizadoEn(rol.actualizadoEn());
    return entity;
  }

  private Rol toDomain(RolEntity rol) {
    boolean activo = rol.getActivo() == null || rol.getActivo();
    return new Rol(
        rol.getId(),
        rol.getNombre(),
        rol.getDescripcion(),
        activo,
        rol.getCreadoEn(),
        rol.getActualizadoEn(),
        List.of()
    );
  }
}
