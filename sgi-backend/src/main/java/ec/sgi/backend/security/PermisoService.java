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
    String rol = currentUserService.getRol();
    if (rol != null && rol.equalsIgnoreCase("ADMIN")) {
      return;
    }
    if (rol == null || rol.isBlank()) {
      throw new ForbiddenException("No tiene permisos para acceder a este recurso");
    }
    Long empresaId = currentUserService.getEmpresaId();
    if (!accionRepository.existsActiveByCodigo(empresaId, permiso)) {
      throw new ForbiddenException("No tiene permisos para acceder a este recurso");
    }
    if (!rolRepository.existsPermiso(empresaId, rol, permiso)) {
      throw new ForbiddenException("No tiene permisos para acceder a este recurso");
    }
  }
}
