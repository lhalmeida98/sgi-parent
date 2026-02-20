package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.ProductoCreateRequest;
import ec.sgi.backend.application.dto.ProductoCreateResult;
import ec.sgi.backend.application.dto.ProductoResult;
import ec.sgi.backend.application.dto.ProductoUpdateRequest;
import ec.sgi.backend.application.dto.ProductoVendibleUpdateRequest;
import ec.sgi.backend.application.port.in.ActualizarProductoCommand;
import ec.sgi.backend.application.port.in.ActualizarProductoUseCase;
import ec.sgi.backend.application.port.in.ActualizarProductoVendibleUseCase;
import ec.sgi.backend.application.port.in.BuscarProductoPorCodigoUseCase;
import ec.sgi.backend.application.port.in.CrearProductoCommand;
import ec.sgi.backend.application.port.in.CrearProductoUseCase;
import ec.sgi.backend.application.port.in.ListarProductosUseCase;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Gestion de productos.")
public class ProductoController {
  private final CrearProductoUseCase crearProductoUseCase;
  private final ListarProductosUseCase listarProductosUseCase;
  private final ActualizarProductoUseCase actualizarProductoUseCase;
  private final ActualizarProductoVendibleUseCase actualizarProductoVendibleUseCase;
  private final BuscarProductoPorCodigoUseCase buscarProductoPorCodigoUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public ProductoController(
      CrearProductoUseCase crearProductoUseCase,
      ListarProductosUseCase listarProductosUseCase,
      ActualizarProductoUseCase actualizarProductoUseCase,
      ActualizarProductoVendibleUseCase actualizarProductoVendibleUseCase,
      BuscarProductoPorCodigoUseCase buscarProductoPorCodigoUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.crearProductoUseCase = crearProductoUseCase;
    this.listarProductosUseCase = listarProductosUseCase;
    this.actualizarProductoUseCase = actualizarProductoUseCase;
    this.actualizarProductoVendibleUseCase = actualizarProductoVendibleUseCase;
    this.buscarProductoPorCodigoUseCase = buscarProductoPorCodigoUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @PostMapping
  @Operation(summary = "Crear producto", description = "Crea un producto para la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Producto creado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<ProductoCreateResult> crear(@Valid @RequestBody ProductoCreateRequest request) {
    permisoService.requirePermiso("PRODUCTOS");
    Long empresaId = currentUserService.getEmpresaId();
    ProductoCreateResult result = crearProductoUseCase.crear(new CrearProductoCommand(
        empresaId,
        request.codigo(),
        request.descripcion(),
        request.precioUnitario(),
        request.categoriaId(),
        request.impuestoId(),
        request.vendible(),
        request.codigoBarras()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  @Operation(summary = "Listar productos", description = "Lista productos de la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de productos"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<ProductoResult>> listar() {
    permisoService.requirePermiso("PRODUCTOS");
    return ResponseEntity.ok(listarProductosUseCase.listar(currentUserService.getEmpresaId()));
  }

  @GetMapping("/buscar")
  @Operation(summary = "Buscar producto por codigo", description = "Busca un producto por codigo o codigo de barras.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Producto encontrado"),
      @ApiResponse(responseCode = "400", description = "Codigo requerido"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Producto no encontrado")
  })
  public ResponseEntity<ProductoResult> buscar(
      @Parameter(description = "Codigo o codigo de barras") @RequestParam("codigo") String codigo
  ) {
    permisoService.requirePermiso("PRODUCTOS");
    ProductoResult result = buscarProductoPorCodigoUseCase.buscar(currentUserService.getEmpresaId(), codigo);
    return ResponseEntity.ok(result);
  }

  @PutMapping("/{productoId}")
  @Operation(summary = "Actualizar producto", description = "Actualiza un producto existente.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Producto actualizado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Producto no encontrado")
  })
  public ResponseEntity<ProductoResult> actualizar(
      @Parameter(description = "ID del producto") @PathVariable Long productoId,
      @Valid @RequestBody ProductoUpdateRequest request
  ) {
    permisoService.requirePermiso("PRODUCTOS");
    ProductoResult result = actualizarProductoUseCase.actualizar(
        currentUserService.getEmpresaId(),
        productoId,
        new ActualizarProductoCommand(
            request.codigo(),
            request.descripcion(),
            request.precioUnitario(),
            request.categoriaId(),
            request.impuestoId(),
            request.vendible(),
            request.codigoBarras()
        )
    );
    return ResponseEntity.ok(result);
  }

  @PutMapping("/{productoId}/vendible")
  @Operation(summary = "Actualizar vendible", description = "Actualiza el estado vendible de un producto.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Producto actualizado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Producto no encontrado")
  })
  public ResponseEntity<ProductoResult> actualizarVendible(
      @Parameter(description = "ID del producto") @PathVariable Long productoId,
      @Valid @RequestBody ProductoVendibleUpdateRequest request
  ) {
    permisoService.requirePermiso("PRODUCTOS");
    ProductoResult result = actualizarProductoVendibleUseCase.actualizarVendible(
        currentUserService.getEmpresaId(),
        productoId,
        request.vendible()
    );
    return ResponseEntity.ok(result);
  }
}
