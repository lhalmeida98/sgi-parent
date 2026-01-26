package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.ProductoCreateRequest;
import ec.sgi.backend.application.dto.ProductoCreateResult;
import ec.sgi.backend.application.dto.ProductoResult;
import ec.sgi.backend.application.dto.ProductoUpdateRequest;
import ec.sgi.backend.application.port.in.ActualizarProductoCommand;
import ec.sgi.backend.application.port.in.ActualizarProductoUseCase;
import ec.sgi.backend.application.port.in.CrearProductoCommand;
import ec.sgi.backend.application.port.in.CrearProductoUseCase;
import ec.sgi.backend.application.port.in.ListarProductosUseCase;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
import ec.sgi.backend.security.Permisos;
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
@RequestMapping("/api/productos")
public class ProductoController {
  private final CrearProductoUseCase crearProductoUseCase;
  private final ListarProductosUseCase listarProductosUseCase;
  private final ActualizarProductoUseCase actualizarProductoUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public ProductoController(
      CrearProductoUseCase crearProductoUseCase,
      ListarProductosUseCase listarProductosUseCase,
      ActualizarProductoUseCase actualizarProductoUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.crearProductoUseCase = crearProductoUseCase;
    this.listarProductosUseCase = listarProductosUseCase;
    this.actualizarProductoUseCase = actualizarProductoUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @PostMapping
  public ResponseEntity<ProductoCreateResult> crear(@Valid @RequestBody ProductoCreateRequest request) {
    permisoService.requirePermiso(Permisos.PRODUCTO_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    ProductoCreateResult result = crearProductoUseCase.crear(new CrearProductoCommand(
        empresaId,
        request.codigo(),
        request.descripcion(),
        request.precioUnitario(),
        request.categoriaId(),
        request.impuestoId(),
        request.codigoBarras()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  public ResponseEntity<List<ProductoResult>> listar() {
    permisoService.requirePermiso(Permisos.PRODUCTO_GESTION);
    return ResponseEntity.ok(listarProductosUseCase.listar(currentUserService.getEmpresaId()));
  }

  @PutMapping("/{productoId}")
  public ResponseEntity<ProductoResult> actualizar(
      @PathVariable Long productoId,
      @Valid @RequestBody ProductoUpdateRequest request
  ) {
    permisoService.requirePermiso(Permisos.PRODUCTO_GESTION);
    ProductoResult result = actualizarProductoUseCase.actualizar(
        currentUserService.getEmpresaId(),
        productoId,
        new ActualizarProductoCommand(
            request.codigo(),
            request.descripcion(),
            request.precioUnitario(),
            request.categoriaId(),
            request.impuestoId(),
            request.codigoBarras()
        )
    );
    return ResponseEntity.ok(result);
  }
}
