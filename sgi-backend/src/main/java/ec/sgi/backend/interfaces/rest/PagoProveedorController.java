package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.PagoProveedorCreateRequest;
import ec.sgi.backend.application.dto.PagoProveedorDetalleRequest;
import ec.sgi.backend.application.dto.PagoProveedorResult;
import ec.sgi.backend.application.port.in.CrearPagoProveedorCommand;
import ec.sgi.backend.application.port.in.CrearPagoProveedorUseCase;
import ec.sgi.backend.application.port.in.ListarPagosProveedorUseCase;
import ec.sgi.backend.application.port.in.PagoProveedorDetalleCommand;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos-proveedor")
@Tag(name = "Pagos Proveedor", description = "Pagos aplicados a cuentas por pagar.")
public class PagoProveedorController {
  private final CrearPagoProveedorUseCase crearPagoProveedorUseCase;
  private final ListarPagosProveedorUseCase listarPagosProveedorUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public PagoProveedorController(
      CrearPagoProveedorUseCase crearPagoProveedorUseCase,
      ListarPagosProveedorUseCase listarPagosProveedorUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.crearPagoProveedorUseCase = crearPagoProveedorUseCase;
    this.listarPagosProveedorUseCase = listarPagosProveedorUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @PostMapping
  @Operation(summary = "Registrar pago", description = "Registra un pago y aplica a cuentas por pagar.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Pago registrado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Proveedor o cuenta no encontrada")
  })
  public ResponseEntity<PagoProveedorResult> crear(@Valid @RequestBody PagoProveedorCreateRequest request) {
    permisoService.requirePermiso("PROVEEDORES");
    Long empresaId = currentUserService.getEmpresaId();
    PagoProveedorResult result = crearPagoProveedorUseCase.crear(new CrearPagoProveedorCommand(
        empresaId,
        request.proveedorId(),
        request.fechaPago(),
        request.formaPago(),
        request.referencia(),
        request.montoTotal(),
        request.observacion(),
        mapDetalles(request.detalles())
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  @Operation(summary = "Listar pagos", description = "Lista pagos a proveedores. Puede filtrar por proveedorId.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de pagos"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<PagoProveedorResult>> listar(
      @Parameter(description = "ID del proveedor") @RequestParam(required = false) Long proveedorId
  ) {
    permisoService.requirePermiso("PROVEEDORES");
    Long empresaId = currentUserService.getEmpresaId();
    return ResponseEntity.ok(listarPagosProveedorUseCase.listar(empresaId, proveedorId));
  }

  private List<PagoProveedorDetalleCommand> mapDetalles(List<PagoProveedorDetalleRequest> detalles) {
    if (detalles == null || detalles.isEmpty()) {
      return Collections.emptyList();
    }
    return detalles.stream()
        .map(detalle -> new PagoProveedorDetalleCommand(
            detalle.cuentaPorPagarId(),
            detalle.montoAplicado()
        ))
        .toList();
  }
}
