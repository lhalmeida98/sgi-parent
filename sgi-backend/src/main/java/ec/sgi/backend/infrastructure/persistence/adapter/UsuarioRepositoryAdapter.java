package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.UsuarioRepository;
import ec.sgi.backend.domain.model.Usuario;
import ec.sgi.backend.infrastructure.persistence.entity.RolEntity;
import ec.sgi.backend.infrastructure.persistence.entity.UsuarioEmpresaEntity;
import ec.sgi.backend.infrastructure.persistence.entity.UsuarioEntity;
import ec.sgi.backend.infrastructure.persistence.entity.UsuarioRolEntity;
import ec.sgi.backend.infrastructure.persistence.repository.RolJpaRepository;
import ec.sgi.backend.infrastructure.persistence.repository.UsuarioEmpresaJpaRepository;
import ec.sgi.backend.infrastructure.persistence.repository.UsuarioJpaRepository;
import ec.sgi.backend.infrastructure.persistence.repository.UsuarioRolJpaRepository;
import ec.sgi.backend.domain.model.UsuarioEmpresa;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepository {
  private final UsuarioJpaRepository usuarioJpaRepository;
  private final UsuarioRolJpaRepository usuarioRolJpaRepository;
  private final UsuarioEmpresaJpaRepository usuarioEmpresaJpaRepository;
  private final RolJpaRepository rolJpaRepository;

  public UsuarioRepositoryAdapter(
      UsuarioJpaRepository usuarioJpaRepository,
      UsuarioRolJpaRepository usuarioRolJpaRepository,
      UsuarioEmpresaJpaRepository usuarioEmpresaJpaRepository,
      RolJpaRepository rolJpaRepository
  ) {
    this.usuarioJpaRepository = usuarioJpaRepository;
    this.usuarioRolJpaRepository = usuarioRolJpaRepository;
    this.usuarioEmpresaJpaRepository = usuarioEmpresaJpaRepository;
    this.rolJpaRepository = rolJpaRepository;
  }

  @Override
  public Usuario save(Usuario usuario) {
    UsuarioEntity saved = usuarioJpaRepository.save(toEntity(usuario));
    usuarioRolJpaRepository.deleteByUsuarioId(saved.getId());
    usuarioRolJpaRepository.flush();
    if (!usuario.roles().isEmpty()) {
      List<UsuarioRolEntity> relaciones = new ArrayList<>();
      Set<Long> rolIds = new java.util.LinkedHashSet<>();
      for (String rol : usuario.roles()) {
        RolEntity rolEntity = rolJpaRepository.findByNombre(rol)
            .orElseThrow(() -> new IllegalStateException("Rol no encontrado: " + rol));
        if (!rolIds.add(rolEntity.getId())) {
          continue;
        }
        UsuarioRolEntity rel = new UsuarioRolEntity();
        rel.setUsuarioId(saved.getId());
        rel.setRolId(rolEntity.getId());
        relaciones.add(rel);
      }
      usuarioRolJpaRepository.saveAll(relaciones);
    }
    usuarioEmpresaJpaRepository.deleteByUsuarioId(saved.getId());
    usuarioEmpresaJpaRepository.flush();
    if (!usuario.empresas().isEmpty()) {
      List<UsuarioEmpresaEntity> empresas = new ArrayList<>();
      Set<Long> empresasIds = new java.util.LinkedHashSet<>();
      for (UsuarioEmpresa empresa : usuario.empresas()) {
        if (!empresasIds.add(empresa.empresaId())) {
          continue;
        }
        UsuarioEmpresaEntity rel = new UsuarioEmpresaEntity();
        rel.setUsuarioId(saved.getId());
        rel.setEmpresaId(empresa.empresaId());
        rel.setPrincipal(empresa.principal());
        empresas.add(rel);
      }
      usuarioEmpresaJpaRepository.saveAll(empresas);
    }
    return toDomain(saved, usuario.roles(), usuario.empresas());
  }

  @Override
  public List<Usuario> findByEmpresaId(Long empresaId) {
    List<Long> usuarioIds = usuarioEmpresaJpaRepository.findUsuarioIdsByEmpresaId(empresaId);
    if (usuarioIds.isEmpty()) {
      return List.of();
    }
    List<UsuarioEntity> usuarios = usuarioJpaRepository.findAllById(usuarioIds);
    if (usuarios.isEmpty()) {
      return List.of();
    }
    Map<Long, List<String>> rolesPorUsuario = resolveRoles(usuarios);
    Map<Long, List<UsuarioEmpresa>> empresasPorUsuario = resolveEmpresas(usuarios);
    return usuarios.stream()
        .map(usuario -> toDomain(
            usuario,
            rolesPorUsuario.getOrDefault(usuario.getId(), List.of()),
            empresasPorUsuario.getOrDefault(usuario.getId(), List.of())
        ))
        .toList();
  }

  @Override
  public Optional<Usuario> findById(Long id) {
    return usuarioJpaRepository.findById(id)
        .map(usuario -> toDomain(
            usuario,
            usuarioRolJpaRepository.findRoleNamesByUsuarioId(usuario.getId()),
            usuarioEmpresaJpaRepository.findEmpresasByUsuarioId(usuario.getId())
        ));
  }

  @Override
  public Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId) {
    if (!usuarioEmpresaJpaRepository.existsByUsuarioIdAndEmpresaId(id, empresaId)) {
      return Optional.empty();
    }
    return usuarioJpaRepository.findById(id)
        .map(usuario -> toDomain(
            usuario,
            usuarioRolJpaRepository.findRoleNamesByUsuarioId(usuario.getId()),
            usuarioEmpresaJpaRepository.findEmpresasByUsuarioId(usuario.getId())
        ));
  }

  @Override
  public Optional<Usuario> findByEmail(String email) {
    return usuarioJpaRepository.findByEmail(email)
        .map(usuario -> toDomain(
            usuario,
            usuarioRolJpaRepository.findRoleNamesByUsuarioId(usuario.getId()),
            usuarioEmpresaJpaRepository.findEmpresasByUsuarioId(usuario.getId())
        ));
  }

  @Override
  public Optional<Usuario> findByUsuario(String usuario) {
    return usuarioJpaRepository.findByUsuario(usuario)
        .map(entity -> toDomain(
            entity,
            usuarioRolJpaRepository.findRoleNamesByUsuarioId(entity.getId()),
            usuarioEmpresaJpaRepository.findEmpresasByUsuarioId(entity.getId())
        ));
  }

  @Override
  public List<Usuario> findAll() {
    List<UsuarioEntity> usuarios = usuarioJpaRepository.findAll();
    if (usuarios.isEmpty()) {
      return List.of();
    }
    Map<Long, List<String>> rolesPorUsuario = resolveRoles(usuarios);
    Map<Long, List<UsuarioEmpresa>> empresasPorUsuario = resolveEmpresas(usuarios);
    return usuarios.stream()
        .map(usuario -> toDomain(
            usuario,
            rolesPorUsuario.getOrDefault(usuario.getId(), List.of()),
            empresasPorUsuario.getOrDefault(usuario.getId(), List.of())
        ))
        .toList();
  }

  @Override
  public void deleteById(Long id) {
    usuarioRolJpaRepository.deleteByUsuarioId(id);
    usuarioEmpresaJpaRepository.deleteByUsuarioId(id);
    usuarioJpaRepository.deleteById(id);
  }

  private Usuario toDomain(UsuarioEntity entity, List<String> roles, List<UsuarioEmpresa> empresas) {
    boolean activo = entity.getActivo() == null || entity.getActivo();
    List<UsuarioEmpresa> empresasFinal = empresas;
    if ((empresasFinal == null || empresasFinal.isEmpty()) && entity.getEmpresaId() != null) {
      empresasFinal = List.of(new UsuarioEmpresa(entity.getEmpresaId(), true));
    }
    return new Usuario(
        entity.getId(),
        entity.getEmpresaId(),
        empresasFinal == null ? List.of() : empresasFinal,
        entity.getNombre(),
        entity.getUsuario(),
        entity.getEmail(),
        entity.getPasswordHash(),
        roles,
        activo,
        entity.getCreadoEn(),
        entity.getActualizadoEn()
    );
  }

  private UsuarioEntity toEntity(Usuario usuario) {
    UsuarioEntity entity = new UsuarioEntity();
    entity.setId(usuario.id());
    entity.setEmpresaId(usuario.empresaId());
    entity.setNombre(usuario.nombre());
    entity.setUsuario(usuario.usuario());
    entity.setEmail(usuario.email());
    entity.setPasswordHash(usuario.passwordHash());
    entity.setActivo(usuario.activo());
    entity.setCreadoEn(usuario.creadoEn());
    entity.setActualizadoEn(usuario.actualizadoEn());
    return entity;
  }

  private Map<Long, List<String>> resolveRoles(List<UsuarioEntity> usuarios) {
    List<Long> usuarioIds = usuarios.stream().map(UsuarioEntity::getId).toList();
    List<UsuarioRolEntity> relaciones = usuarioRolJpaRepository.findByUsuarioIdIn(usuarioIds);
    if (relaciones.isEmpty()) {
      return Map.of();
    }
    Map<Long, List<Long>> rolIdsPorUsuario = new HashMap<>();
    Set<Long> rolIds = relaciones.stream().map(UsuarioRolEntity::getRolId).collect(Collectors.toSet());
    for (UsuarioRolEntity rel : relaciones) {
      rolIdsPorUsuario.computeIfAbsent(rel.getUsuarioId(), key -> new ArrayList<>())
          .add(rel.getRolId());
    }
    Map<Long, String> nombresPorRolId = rolJpaRepository.findAllById(rolIds).stream()
        .filter(rol -> rol.getActivo() == null || rol.getActivo())
        .collect(Collectors.toMap(RolEntity::getId, RolEntity::getNombre));
    Map<Long, List<String>> rolesPorUsuario = new HashMap<>();
    for (Map.Entry<Long, List<Long>> entry : rolIdsPorUsuario.entrySet()) {
      List<String> nombres = entry.getValue().stream()
          .map(nombresPorRolId::get)
          .filter(nombre -> nombre != null && !nombre.isBlank())
          .toList();
      rolesPorUsuario.put(entry.getKey(), nombres);
    }
    return rolesPorUsuario;
  }

  private Map<Long, List<UsuarioEmpresa>> resolveEmpresas(List<UsuarioEntity> usuarios) {
    List<Long> usuarioIds = usuarios.stream().map(UsuarioEntity::getId).toList();
    List<UsuarioEmpresaEntity> relaciones = usuarioEmpresaJpaRepository.findByUsuarioIdIn(usuarioIds);
    if (relaciones.isEmpty()) {
      return Map.of();
    }
    Map<Long, List<UsuarioEmpresa>> empresasPorUsuario = new HashMap<>();
    for (UsuarioEmpresaEntity rel : relaciones) {
      if (rel.getEmpresaId() == null) {
        continue;
      }
      empresasPorUsuario.computeIfAbsent(rel.getUsuarioId(), key -> new ArrayList<>())
          .add(new UsuarioEmpresa(
              rel.getEmpresaId(),
              rel.getPrincipal() != null && rel.getPrincipal()
          ));
    }
    return empresasPorUsuario;
  }
}
