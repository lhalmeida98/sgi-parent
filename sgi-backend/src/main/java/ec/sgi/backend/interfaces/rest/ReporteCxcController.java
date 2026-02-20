package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.CxcAgingResumenResult;
import ec.sgi.backend.application.port.in.ReporteCxcAgingUseCase;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/reportes/cxc")
@Tag(name = "Reportes CxC", description = "Reportes de cuentas por cobrar.")
public class ReporteCxcController {
  private final ReporteCxcAgingUseCase reporteCxcAgingUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public ReporteCxcController(
      ReporteCxcAgingUseCase reporteCxcAgingUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.reporteCxcAgingUseCase = reporteCxcAgingUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @GetMapping("/aging")
  @Operation(summary = "Resumen de vencidas/por vencer", description = "Agrupa cuentas por cobrar por dias de vencimiento.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Resumen generado"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<CxcAgingResumenResult> resumen(
      @Parameter(description = "ID del cliente") @RequestParam(required = false) Long clienteId
  ) {
    permisoService.requirePermiso("CLIENTES");
    Long empresaId = currentUserService.getEmpresaId();
    return ResponseEntity.ok(reporteCxcAgingUseCase.resumen(empresaId, clienteId));
  }
}
