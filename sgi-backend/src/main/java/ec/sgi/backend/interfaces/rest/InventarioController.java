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
  public ResponseEntity<List<InventarioResult>> listar() {
    permisoService.requirePermiso(Permisos.INVENTARIO_GESTION);
    return ResponseEntity.ok(listarInventarioUseCase.listar(currentUserService.getEmpresaId()));
  }
}
