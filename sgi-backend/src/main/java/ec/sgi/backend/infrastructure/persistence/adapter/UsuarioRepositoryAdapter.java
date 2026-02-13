package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.UsuarioRepository;
import ec.sgi.backend.domain.model.Usuario;
import ec.sgi.backend.infrastructure.persistence.entity.UsuarioEntity;
import ec.sgi.backend.infrastructure.persistence.repository.UsuarioJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepository {
  private final UsuarioJpaRepository usuarioJpaRepository;

  public UsuarioRepositoryAdapter(UsuarioJpaRepository usuarioJpaRepository) {
    this.usuarioJpaRepository = usuarioJpaRepository;
  }

  @Override
  public Usuario save(Usuario usuario) {
    return toDomain(usuarioJpaRepository.save(toEntity(usuario)));
  }

  @Override
  public List<Usuario> findByEmpresaId(Long empresaId) {
    return usuarioJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId) {
    return usuarioJpaRepository.findByIdAndEmpresaId(id, empresaId).map(this::toDomain);
  }

  @Override
  public Optional<Usuario> findByEmail(String email) {
    return usuarioJpaRepository.findByEmail(email).map(this::toDomain);
  }

  @Override
  public Optional<Usuario> findByUsuario(String usuario) {
    return usuarioJpaRepository.findByUsuario(usuario).map(this::toDomain);
  }

  private Usuario toDomain(UsuarioEntity entity) {
    boolean activo = entity.getActivo() == null || entity.getActivo();
    return new Usuario(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getNombre(),
        entity.getUsuario(),
        entity.getEmail(),
        entity.getPasswordHash(),
        entity.getRol(),
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
    entity.setRol(usuario.rol());
    entity.setActivo(usuario.activo());
    entity.setCreadoEn(usuario.creadoEn());
    entity.setActualizadoEn(usuario.actualizadoEn());
    return entity;
  }
}
