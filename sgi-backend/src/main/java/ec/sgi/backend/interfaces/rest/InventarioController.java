package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.InventarioCreateRequest;
import ec.sgi.backend.application.dto.InventarioCreateResult;
import ec.sgi.backend.application.dto.InventarioDetalleResult;
import ec.sgi.backend.application.dto.InventarioProductoDisponibleResult;
import ec.sgi.backend.application.dto.InventarioResumenResult;
import ec.sgi.backend.application.port.in.BuscarProductoDisponiblePorBodegaUseCase;
import ec.sgi.backend.application.port.in.BuscarProductoDisponiblePorIdUseCase;
import ec.sgi.backend.application.port.in.ConsultarInventarioProductoBodegaUseCase;
import ec.sgi.backend.application.port.in.CrearInventarioCommand;
import ec.sgi.backend.application.port.in.CrearInventarioUseCase;
import ec.sgi.backend.application.port.in.ListarInventarioUseCase;
import ec.sgi.backend.application.port.in.ListarProductosDisponiblesPorBodegaUseCase;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventarios")
@Tag(name = "Inventario", description = "Gestion de inventarios.")
public class InventarioController {
  private final CrearInventarioUseCase crearInventarioUseCase;
  private final ListarInventarioUseCase listarInventarioUseCase;
  private final ConsultarInventarioProductoBodegaUseCase consultarInventarioProductoBodegaUseCase;
  private final ListarProductosDisponiblesPorBodegaUseCase listarProductosDisponiblesPorBodegaUseCase;
  private final BuscarProductoDisponiblePorBodegaUseCase buscarProductoDisponiblePorBodegaUseCase;
  private final BuscarProductoDisponiblePorIdUseCase buscarProductoDisponiblePorIdUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public InventarioController(
      CrearInventarioUseCase crearInventarioUseCase,
      ListarInventarioUseCase listarInventarioUseCase,
      ConsultarInventarioProductoBodegaUseCase consultarInventarioProductoBodegaUseCase,
      ListarProductosDisponiblesPorBodegaUseCase listarProductosDisponiblesPorBodegaUseCase,
      BuscarProductoDisponiblePorBodegaUseCase buscarProductoDisponiblePorBodegaUseCase,
      BuscarProductoDisponiblePorIdUseCase buscarProductoDisponiblePorIdUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.crearInventarioUseCase = crearInventarioUseCase;
    this.listarInventarioUseCase = listarInventarioUseCase;
    this.consultarInventarioProductoBodegaUseCase = consultarInventarioProductoBodegaUseCase;
    this.listarProductosDisponiblesPorBodegaUseCase = listarProductosDisponiblesPorBodegaUseCase;
    this.buscarProductoDisponiblePorBodegaUseCase = buscarProductoDisponiblePorBodegaUseCase;
    this.buscarProductoDisponiblePorIdUseCase = buscarProductoDisponiblePorIdUseCase;
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
        request.bodegaId(),
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
  @Operation(summary = "Listar inventario", description = "Lista inventario global y por bodega de la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de inventario"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<InventarioResumenResult>> listar() {
    permisoService.requirePermiso(Permisos.INVENTARIO_GESTION);
    return ResponseEntity.ok(listarInventarioUseCase.listar(currentUserService.getEmpresaId()));
  }

  @GetMapping("/producto/{productoId}/bodega/{bodegaId}")
  @Operation(summary = "Consultar inventario por producto y bodega", description = "Devuelve stock global y stock de una bodega.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Inventario encontrado"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
  })
  public ResponseEntity<InventarioDetalleResult> consultar(
      @Parameter(description = "ID del producto") @PathVariable Long productoId,
      @Parameter(description = "ID de la bodega") @PathVariable Long bodegaId
  ) {
    permisoService.requirePermiso(Permisos.INVENTARIO_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    return ResponseEntity.ok(consultarInventarioProductoBodegaUseCase.consultar(empresaId, productoId, bodegaId));
  }

  @GetMapping("/bodega/{bodegaId}/productos-disponibles")
  @Operation(
      summary = "Listar productos disponibles por bodega",
      description = "Lista productos con stock disponible en una bodega, incluyendo atributos del producto."
  )
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de productos disponibles"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Bodega no encontrada")
  })
  public ResponseEntity<List<InventarioProductoDisponibleResult>> listarDisponibles(
      @Parameter(description = "ID de la bodega") @PathVariable Long bodegaId
  ) {
    permisoService.requirePermiso(Permisos.INVENTARIO_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    return ResponseEntity.ok(listarProductosDisponiblesPorBodegaUseCase.listar(empresaId, bodegaId));
  }

  @GetMapping("/bodega/{bodegaId}/productos-disponibles/buscar")
  @Operation(
      summary = "Buscar producto disponible por codigo o codigo de barras",
      description = "Busca un producto por codigo o codigo de barras dentro de una bodega y devuelve stock disponible."
  )
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Producto disponible encontrado"),
      @ApiResponse(responseCode = "400", description = "Codigo requerido"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Producto o inventario no encontrado")
  })
  public ResponseEntity<InventarioProductoDisponibleResult> buscarDisponible(
      @Parameter(description = "ID de la bodega") @PathVariable Long bodegaId,
      @Parameter(description = "Codigo o codigo de barras") @RequestParam("codigo") String codigo
  ) {
    permisoService.requirePermiso(Permisos.INVENTARIO_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    return ResponseEntity.ok(buscarProductoDisponiblePorBodegaUseCase.buscar(empresaId, bodegaId, codigo));
  }

  @GetMapping("/bodega/{bodegaId}/productos-disponibles/{productoId}")
  @Operation(
      summary = "Buscar producto disponible por ID",
      description = "Busca un producto por ID dentro de una bodega y devuelve stock disponible."
  )
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Producto disponible encontrado"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Producto o inventario no encontrado")
  })
  public ResponseEntity<InventarioProductoDisponibleResult> buscarDisponiblePorId(
      @Parameter(description = "ID de la bodega") @PathVariable Long bodegaId,
      @Parameter(description = "ID del producto") @PathVariable Long productoId
  ) {
    permisoService.requirePermiso(Permisos.INVENTARIO_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    return ResponseEntity.ok(buscarProductoDisponiblePorIdUseCase.buscar(empresaId, bodegaId, productoId));
  }
}
