package ec.sgi.backend.security;

import ec.sgi.backend.infrastructure.persistence.entity.UsuarioEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UsuarioPrincipal implements UserDetails {
  private final Long id;
  private final Long empresaId;
  private final String nombre;
  private final String email;
  private final String passwordHash;
  private final List<String> roles;
  private final boolean activo;

  public UsuarioPrincipal(UsuarioEntity entity, List<String> roles) {
    this.id = entity.getId();
    this.empresaId = entity.getEmpresaId();
    this.nombre = entity.getNombre();
    this.email = entity.getEmail();
    this.passwordHash = entity.getPasswordHash();
    this.roles = roles == null ? List.of() : roles;
    this.activo = entity.getActivo() == null || entity.getActivo();
  }

  public Long getId() {
    return id;
  }

  public Long getEmpresaId() {
    return empresaId;
  }

  public String getNombre() {
    return nombre;
  }

  public List<String> getRoles() {
    return roles;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (roles == null || roles.isEmpty()) {
      return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    return roles.stream()
        .filter(role -> role != null && !role.isBlank())
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .toList();
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return activo;
  }
}
