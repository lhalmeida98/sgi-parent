package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.CobroClienteCreateRequest;
import ec.sgi.backend.application.dto.CobroClienteDetalleRequest;
import ec.sgi.backend.application.dto.CobroClienteResult;
import ec.sgi.backend.application.port.in.CobroClienteDetalleCommand;
import ec.sgi.backend.application.port.in.CrearCobroClienteCommand;
import ec.sgi.backend.application.port.in.CrearCobroClienteUseCase;
import ec.sgi.backend.application.port.in.ListarCobrosClienteUseCase;
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
@RequestMapping("/api/cobros-cliente")
@Tag(name = "Cobros Cliente", description = "Cobros aplicados a cuentas por cobrar.")
public class CobroClienteController {
  private final CrearCobroClienteUseCase crearCobroClienteUseCase;
  private final ListarCobrosClienteUseCase listarCobrosClienteUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public CobroClienteController(
      CrearCobroClienteUseCase crearCobroClienteUseCase,
      ListarCobrosClienteUseCase listarCobrosClienteUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.crearCobroClienteUseCase = crearCobroClienteUseCase;
    this.listarCobrosClienteUseCase = listarCobrosClienteUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @PostMapping
  @Operation(summary = "Registrar cobro", description = "Registra un cobro y aplica a cuentas por cobrar.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Cobro registrado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Cliente o cuenta no encontrada")
  })
  public ResponseEntity<CobroClienteResult> crear(@Valid @RequestBody CobroClienteCreateRequest request) {
    permisoService.requirePermiso("CLIENTES");
    Long empresaId = currentUserService.getEmpresaId();
    CobroClienteResult result = crearCobroClienteUseCase.crear(new CrearCobroClienteCommand(
        empresaId,
        request.clienteId(),
        request.fecha(),
        request.formaPago(),
        request.referencia(),
        request.montoTotal(),
        request.observacion(),
        mapDetalles(request.detalles())
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  @Operation(summary = "Listar cobros", description = "Lista cobros a clientes. Puede filtrar por clienteId.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de cobros"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<CobroClienteResult>> listar(
      @Parameter(description = "ID del cliente") @RequestParam(required = false) Long clienteId
  ) {
    permisoService.requirePermiso("CLIENTES");
    Long empresaId = currentUserService.getEmpresaId();
    return ResponseEntity.ok(listarCobrosClienteUseCase.listar(empresaId, clienteId));
  }

  private List<CobroClienteDetalleCommand> mapDetalles(List<CobroClienteDetalleRequest> detalles) {
    if (detalles == null || detalles.isEmpty()) {
      return Collections.emptyList();
    }
    return detalles.stream()
        .map(detalle -> new CobroClienteDetalleCommand(
            detalle.cuentaPorCobrarId(),
            detalle.montoAplicado()
        ))
        .toList();
  }
}
