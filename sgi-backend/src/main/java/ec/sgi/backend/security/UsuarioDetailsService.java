package ec.sgi.backend.security;

import ec.sgi.backend.infrastructure.persistence.repository.UsuarioJpaRepository;
import ec.sgi.backend.infrastructure.persistence.repository.UsuarioRolJpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {
  private final UsuarioJpaRepository usuarioJpaRepository;
  private final UsuarioRolJpaRepository usuarioRolJpaRepository;

  public UsuarioDetailsService(
      UsuarioJpaRepository usuarioJpaRepository,
      UsuarioRolJpaRepository usuarioRolJpaRepository
  ) {
    this.usuarioJpaRepository = usuarioJpaRepository;
    this.usuarioRolJpaRepository = usuarioRolJpaRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    String value = username == null ? "" : username.trim();
    return usuarioJpaRepository.findByEmailOrUsuario(value, value)
        .map(entity -> new UsuarioPrincipal(
            entity,
            usuarioRolJpaRepository.findRoleNamesByUsuarioId(entity.getId())
        ))
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
  }
}
