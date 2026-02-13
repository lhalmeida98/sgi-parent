package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.ProveedorCreateRequest;
import ec.sgi.backend.application.dto.ProveedorCreateResult;
import ec.sgi.backend.application.dto.ProveedorResult;
import ec.sgi.backend.application.dto.ProveedorSriConsultaResult;
import ec.sgi.backend.application.dto.ProveedorUpdateRequest;
import ec.sgi.backend.application.port.in.ActualizarProveedorCommand;
import ec.sgi.backend.application.port.in.ActualizarProveedorUseCase;
import ec.sgi.backend.application.port.in.ConsultarProveedorSriUseCase;
import ec.sgi.backend.application.port.in.CrearProveedorCommand;
import ec.sgi.backend.application.port.in.CrearProveedorUseCase;
import ec.sgi.backend.application.port.in.EliminarProveedorUseCase;
import ec.sgi.backend.application.port.in.ListarProveedoresUseCase;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
import ec.sgi.backend.security.Permisos;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores", description = "Gestion de proveedores.")
public class ProveedorController {
  private final CrearProveedorUseCase crearProveedorUseCase;
  private final ActualizarProveedorUseCase actualizarProveedorUseCase;
  private final ListarProveedoresUseCase listarProveedoresUseCase;
  private final EliminarProveedorUseCase eliminarProveedorUseCase;
  private final ConsultarProveedorSriUseCase consultarProveedorSriUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public ProveedorController(
      CrearProveedorUseCase crearProveedorUseCase,
      ActualizarProveedorUseCase actualizarProveedorUseCase,
      ListarProveedoresUseCase listarProveedoresUseCase,
      EliminarProveedorUseCase eliminarProveedorUseCase,
      ConsultarProveedorSriUseCase consultarProveedorSriUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.crearProveedorUseCase = crearProveedorUseCase;
    this.actualizarProveedorUseCase = actualizarProveedorUseCase;
    this.listarProveedoresUseCase = listarProveedoresUseCase;
    this.eliminarProveedorUseCase = eliminarProveedorUseCase;
    this.consultarProveedorSriUseCase = consultarProveedorSriUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @PostMapping
  @Operation(summary = "Crear proveedor", description = "Crea un proveedor para la empresa actual. Si el tipo es RUC, consulta SRI para validar datos.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Proveedor creado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<ProveedorCreateResult> crear(@Valid @RequestBody ProveedorCreateRequest request) {
    permisoService.requirePermiso(Permisos.PROVEEDOR_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    ProveedorCreateResult result = crearProveedorUseCase.crear(new CrearProveedorCommand(
        empresaId,
        request.tipoIdentificacion(),
        request.identificacion(),
        request.razonSocial(),
        request.nombreComercial(),
        request.email(),
        request.telefono(),
        request.direccion(),
        request.condicionesPago(),
        Boolean.TRUE.equals(request.activo())
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  @Operation(summary = "Listar proveedores", description = "Lista proveedores de la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de proveedores"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<ProveedorResult>> listar() {
    permisoService.requirePermiso(Permisos.PROVEEDOR_GESTION);
    return ResponseEntity.ok(listarProveedoresUseCase.listar(currentUserService.getEmpresaId()));
  }

  @GetMapping("/consulta-sri")
  @Operation(summary = "Consultar proveedor en SRI", description = "Consulta datos del proveedor por identificacion.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Consulta realizada"),
      @ApiResponse(responseCode = "400", description = "Identificacion requerida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<ProveedorSriConsultaResult> consultarSri(
      @Parameter(description = "Numero de identificacion (RUC/Cedula)") @RequestParam String identificacion
  ) {
    permisoService.requirePermiso(Permisos.PROVEEDOR_GESTION);
    ProveedorSriConsultaResult result = consultarProveedorSriUseCase.consultar(identificacion);
    if (!result.encontrado() && "Identificacion requerida".equalsIgnoreCase(result.mensaje())) {
      return ResponseEntity.badRequest().body(result);
    }
    return ResponseEntity.ok(result);
  }

  @PutMapping("/{proveedorId}")
  @Operation(summary = "Actualizar proveedor", description = "Actualiza un proveedor existente.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Proveedor actualizado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
  })
  public ResponseEntity<ProveedorResult> actualizar(
      @Parameter(description = "ID del proveedor") @PathVariable Long proveedorId,
      @Valid @RequestBody ProveedorUpdateRequest request
  ) {
    permisoService.requirePermiso(Permisos.PROVEEDOR_GESTION);
    ProveedorResult result = actualizarProveedorUseCase.actualizar(
        currentUserService.getEmpresaId(),
        proveedorId,
        new ActualizarProveedorCommand(
            request.razonSocial(),
            request.nombreComercial(),
            request.email(),
            request.telefono(),
            request.direccion(),
            request.condicionesPago(),
            Boolean.TRUE.equals(request.activo())
        )
    );
    return ResponseEntity.ok(result);
  }

  @DeleteMapping("/{proveedorId}")
  @Operation(summary = "Eliminar proveedor", description = "Inactiva un proveedor existente.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Proveedor inactivado"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
  })
  public ResponseEntity<Void> eliminar(
      @Parameter(description = "ID del proveedor") @PathVariable Long proveedorId
  ) {
    permisoService.requirePermiso(Permisos.PROVEEDOR_GESTION);
    eliminarProveedorUseCase.eliminar(currentUserService.getEmpresaId(), proveedorId);
    return ResponseEntity.noContent().build();
  }
}
