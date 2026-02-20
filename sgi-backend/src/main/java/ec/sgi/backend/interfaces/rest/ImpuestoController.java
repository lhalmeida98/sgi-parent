package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.ImpuestoCreateRequest;
import ec.sgi.backend.application.dto.ImpuestoCreateResult;
import ec.sgi.backend.application.dto.ImpuestoResult;
import ec.sgi.backend.application.dto.ImpuestoUpdateRequest;
import ec.sgi.backend.application.port.in.ActualizarImpuestoCommand;
import ec.sgi.backend.application.port.in.ActualizarImpuestoUseCase;
import ec.sgi.backend.application.port.in.CrearImpuestoCommand;
import ec.sgi.backend.application.port.in.CrearImpuestoUseCase;
import ec.sgi.backend.application.port.in.ListarImpuestosUseCase;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/impuestos")
@Tag(name = "Impuestos", description = "Gestion de impuestos.")
public class ImpuestoController {
  private final CrearImpuestoUseCase crearImpuestoUseCase;
  private final ListarImpuestosUseCase listarImpuestosUseCase;
  private final ActualizarImpuestoUseCase actualizarImpuestoUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public ImpuestoController(
      CrearImpuestoUseCase crearImpuestoUseCase,
      ListarImpuestosUseCase listarImpuestosUseCase,
      ActualizarImpuestoUseCase actualizarImpuestoUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.crearImpuestoUseCase = crearImpuestoUseCase;
    this.listarImpuestosUseCase = listarImpuestosUseCase;
    this.actualizarImpuestoUseCase = actualizarImpuestoUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @PostMapping
  @Operation(summary = "Crear impuesto", description = "Crea un impuesto para la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Impuesto creado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<ImpuestoCreateResult> crear(@Valid @RequestBody ImpuestoCreateRequest request) {
    permisoService.requirePermiso("IMPUESTOS");
    Long empresaId = currentUserService.getEmpresaId();
    ImpuestoCreateResult result = crearImpuestoUseCase.crear(new CrearImpuestoCommand(
        empresaId,
        request.codigo(),
        request.codigoPorcentaje(),
        request.tarifa(),
        request.descripcion(),
        request.activo()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  @Operation(summary = "Listar impuestos", description = "Lista impuestos de la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de impuestos"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<ImpuestoResult>> listar() {
    permisoService.requireAnyPermiso("IMPUESTOS", "PRODUCTOS");
    return ResponseEntity.ok(listarImpuestosUseCase.listar(currentUserService.getEmpresaId()));
  }

  @PutMapping("/{impuestoId}")
  @Operation(summary = "Actualizar impuesto", description = "Actualiza un impuesto existente.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Impuesto actualizado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Impuesto no encontrado")
  })
  public ResponseEntity<ImpuestoResult> actualizar(
      @Parameter(description = "ID del impuesto") @PathVariable Long impuestoId,
      @Valid @RequestBody ImpuestoUpdateRequest request
  ) {
    permisoService.requirePermiso("IMPUESTOS");
    ImpuestoResult result = actualizarImpuestoUseCase.actualizar(
        currentUserService.getEmpresaId(),
        impuestoId,
        new ActualizarImpuestoCommand(
        request.codigo(),
        request.codigoPorcentaje(),
        request.tarifa(),
        request.descripcion(),
        request.activo()
    ));
    return ResponseEntity.ok(result);
  }
}
