package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.DashboardResumenResult;
import ec.sgi.backend.application.port.in.ObtenerDashboardResumenUseCase;
import ec.sgi.backend.application.exception.ForbiddenException;
import ec.sgi.backend.application.port.out.UsuarioRepository;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Metricas para el dashboard.")
public class DashboardController {
  private final ObtenerDashboardResumenUseCase obtenerDashboardResumenUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;
  private final UsuarioRepository usuarioRepository;

  public DashboardController(
      ObtenerDashboardResumenUseCase obtenerDashboardResumenUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService,
      UsuarioRepository usuarioRepository
  ) {
    this.obtenerDashboardResumenUseCase = obtenerDashboardResumenUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
    this.usuarioRepository = usuarioRepository;
  }

  @GetMapping("/resumen")
  @Operation(
          summary = "Resumen dashboard",
          description = "Metricas agregadas del dashboard por empresa."
  )
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponse(responseCode = "200", description = "Resumen generado")
  @ApiResponse(responseCode = "401", description = "No autorizado")
  @ApiResponse(responseCode = "403", description = "Sin permisos")
  public ResponseEntity<DashboardResumenResult> resumen(
          @RequestParam(required = false) Long empresaId
  ) {
      permisoService.requirePermiso("DASHBOARD");
      Long targetEmpresaId = resolveEmpresaId(empresaId);
      return ResponseEntity.ok(obtenerDashboardResumenUseCase.resumen(targetEmpresaId));
  }

  private Long resolveEmpresaId(Long empresaId) {
    if (empresaId == null) {
      return currentUserService.getEmpresaId();
    }
    if (currentUserService.isAdmin()) {
      return empresaId;
    }
    Long usuarioId = currentUserService.getUsuarioId();
    boolean pertenece = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId).isPresent();
    if (!pertenece) {
      throw new ForbiddenException("Empresa no permitida para el usuario");
    }
    return empresaId;
  }
}
