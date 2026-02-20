package ec.sgi.backend.security;

import ec.sgi.backend.application.exception.ForbiddenException;
import ec.sgi.backend.application.port.out.AccionRepository;
import ec.sgi.backend.application.port.out.RolRepository;
import org.springframework.stereotype.Service;

@Service
public class PermisoService {
  private final CurrentUserService currentUserService;
  private final RolRepository rolRepository;
  private final AccionRepository accionRepository;

  public PermisoService(
      CurrentUserService currentUserService,
      RolRepository rolRepository,
      AccionRepository accionRepository
  ) {
    this.currentUserService = currentUserService;
    this.rolRepository = rolRepository;
    this.accionRepository = accionRepository;
  }

  public void requirePermiso(String permiso) {
    requireAnyPermiso(permiso);
  }

  public void requireAnyPermiso(String... permisos) {
    java.util.List<String> roles = currentUserService.getRoles();
    if (roles != null && roles.stream().anyMatch(role -> role != null && role.equalsIgnoreCase("ADMIN"))) {
      return;
    }
    if (roles == null || roles.isEmpty()) {
      throw new ForbiddenException("No tiene permisos para acceder a este recurso");
    }
    java.util.List<String> solicitados = java.util.Arrays.stream(permisos)
        .filter(p -> p != null && !p.isBlank())
        .toList();
    if (solicitados.isEmpty()) {
      throw new ForbiddenException("No tiene permisos para acceder a este recurso");
    }
    boolean existePermisoActivo = solicitados.stream()
        .anyMatch(accionRepository::existsActiveByCodigo);
    if (!existePermisoActivo) {
      throw new ForbiddenException("No tiene permisos para acceder a este recurso");
    }
    java.util.List<String> permisosAsignados = rolRepository.findPermisosByRoles(roles);
    boolean autorizado = permisosAsignados.stream()
        .filter(codigo -> codigo != null && !codigo.isBlank())
        .anyMatch(codigo -> solicitados.stream().anyMatch(req -> req.equalsIgnoreCase(codigo)));
    if (!autorizado) {
      throw new ForbiddenException("No tiene permisos para acceder a este recurso");
    }
  }
}
