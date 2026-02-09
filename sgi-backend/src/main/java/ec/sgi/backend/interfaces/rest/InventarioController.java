package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.InventarioCreateRequest;
import ec.sgi.backend.application.dto.InventarioCreateResult;
import ec.sgi.backend.application.dto.InventarioResult;
import ec.sgi.backend.application.port.in.CrearInventarioCommand;
import ec.sgi.backend.application.port.in.CrearInventarioUseCase;
import ec.sgi.backend.application.port.in.ListarInventarioUseCase;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
import ec.sgi.backend.security.Permisos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventarios")
@Tag(name = "Inventario", description = "Gestion de inventarios.")
public class InventarioController {
  private final CrearInventarioUseCase crearInventarioUseCase;
  private final ListarInventarioUseCase listarInventarioUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public InventarioController(
      CrearInventarioUseCase crearInventarioUseCase,
      ListarInventarioUseCase listarInventarioUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.crearInventarioUseCase = crearInventarioUseCase;
    this.listarInventarioUseCase = listarInventarioUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @PostMapping
  @Operation(summary = "Crear inventario", description = "Crea registro de inventario para un producto.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Inventario creado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<InventarioCreateResult> crear(@Valid @RequestBody InventarioCreateRequest request) {
    permisoService.requirePermiso(Permisos.INVENTARIO_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    InventarioCreateResult result = crearInventarioUseCase.crear(new CrearInventarioCommand(
        empresaId,
        request.productoId(),
        request.stockActual(),
        request.stockMinimo(),
        request.stockMaximo(),
        request.ubicacion(),
        request.costoPromedio()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  @Operation(summary = "Listar inventario", description = "Lista inventario de la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de inventario"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<InventarioResult>> listar() {
    permisoService.requirePermiso(Permisos.INVENTARIO_GESTION);
    return ResponseEntity.ok(listarInventarioUseCase.listar(currentUserService.getEmpresaId()));
  }
}
