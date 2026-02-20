package ec.sgi.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {
  public Long getEmpresaId() {
    UsuarioPrincipal principal = getPrincipal();
    return principal.getEmpresaId();
  }

  public java.util.List<String> getRoles() {
    UsuarioPrincipal principal = getPrincipal();
    return principal.getRoles();
  }

  public boolean isAdmin() {
    java.util.List<String> roles = getRoles();
    return roles != null && roles.stream().anyMatch(role -> role != null && role.equalsIgnoreCase("ADMIN"));
  }

  private UsuarioPrincipal getPrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
      throw new IllegalStateException("Usuario no autenticado");
    }
    return principal;
  }
}
