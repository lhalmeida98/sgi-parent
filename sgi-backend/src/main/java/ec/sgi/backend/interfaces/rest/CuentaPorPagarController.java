package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.CuentaPorPagarResult;
import ec.sgi.backend.application.port.in.ListarCuentasPorPagarUseCase;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cxp")
@Tag(name = "Cuentas por Pagar", description = "Consulta de cuentas por pagar.")
public class CuentaPorPagarController {
  private final ListarCuentasPorPagarUseCase listarCuentasPorPagarUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public CuentaPorPagarController(
      ListarCuentasPorPagarUseCase listarCuentasPorPagarUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.listarCuentasPorPagarUseCase = listarCuentasPorPagarUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @GetMapping
  @Operation(summary = "Listar cuentas por pagar", description = "Lista cuentas por pagar. Puede filtrar por proveedorId. "
      + "Incluye el numero y tipo del documento para mostrar en UI.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de cuentas"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<CuentaPorPagarResult>> listar(
      @Parameter(description = "ID del proveedor") @RequestParam(required = false) Long proveedorId
  ) {
    permisoService.requirePermiso("PROVEEDORES");
    Long empresaId = currentUserService.getEmpresaId();
    return ResponseEntity.ok(listarCuentasPorPagarUseCase.listar(empresaId, proveedorId));
  }
}
