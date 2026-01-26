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

  public String getRol() {
    UsuarioPrincipal principal = getPrincipal();
    return principal.getRol();
  }

  private UsuarioPrincipal getPrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
      throw new IllegalStateException("Usuario no autenticado");
    }
    return principal;
  }
}
