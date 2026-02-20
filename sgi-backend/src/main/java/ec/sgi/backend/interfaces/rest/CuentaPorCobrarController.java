package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.CuentaPorCobrarResult;
import ec.sgi.backend.application.port.in.ListarCuentasPorCobrarUseCase;
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
@RequestMapping("/api/cxc")
@Tag(name = "Cuentas por Cobrar", description = "Consulta de cuentas por cobrar.")
public class CuentaPorCobrarController {
  private final ListarCuentasPorCobrarUseCase listarCuentasPorCobrarUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public CuentaPorCobrarController(
      ListarCuentasPorCobrarUseCase listarCuentasPorCobrarUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.listarCuentasPorCobrarUseCase = listarCuentasPorCobrarUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @GetMapping
  @Operation(summary = "Listar cuentas por cobrar", description = "Lista cuentas por cobrar. Puede filtrar por clienteId."
      + " Incluye el numero de factura para mostrar en UI.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de cuentas"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<CuentaPorCobrarResult>> listar(
      @Parameter(description = "ID del cliente") @RequestParam(required = false) Long clienteId
  ) {
    permisoService.requirePermiso("CLIENTES");
    Long empresaId = currentUserService.getEmpresaId();
    return ResponseEntity.ok(listarCuentasPorCobrarUseCase.listar(empresaId, clienteId));
  }
}
