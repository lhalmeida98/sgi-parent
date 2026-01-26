package ec.sgi.backend.security;

import ec.sgi.backend.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {
  private final UsuarioJpaRepository usuarioJpaRepository;

  public UsuarioDetailsService(UsuarioJpaRepository usuarioJpaRepository) {
    this.usuarioJpaRepository = usuarioJpaRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    return usuarioJpaRepository.findByEmail(username)
        .map(UsuarioPrincipal::new)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
  }
}
