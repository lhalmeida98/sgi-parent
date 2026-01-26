package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.BodegaCreateRequest;
import ec.sgi.backend.application.dto.BodegaCreateResult;
import ec.sgi.backend.application.dto.BodegaResult;
import ec.sgi.backend.application.port.in.CrearBodegaCommand;
import ec.sgi.backend.application.port.in.CrearBodegaUseCase;
import ec.sgi.backend.application.port.in.ListarBodegasUseCase;
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
@RequestMapping("/api/bodegas")
public class BodegaController {
  private final CrearBodegaUseCase crearBodegaUseCase;
  private final ListarBodegasUseCase listarBodegasUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public BodegaController(
      CrearBodegaUseCase crearBodegaUseCase,
      ListarBodegasUseCase listarBodegasUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.crearBodegaUseCase = crearBodegaUseCase;
    this.listarBodegasUseCase = listarBodegasUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @PostMapping
  public ResponseEntity<BodegaCreateResult> crear(@Valid @RequestBody BodegaCreateRequest request) {
    permisoService.requirePermiso(Permisos.BODEGA_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    BodegaCreateResult result = crearBodegaUseCase.crear(new CrearBodegaCommand(
        empresaId,
        request.nombre(),
        request.descripcion(),
        request.direccion(),
        request.activa()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  public ResponseEntity<List<BodegaResult>> listar() {
    permisoService.requirePermiso(Permisos.BODEGA_GESTION);
    return ResponseEntity.ok(listarBodegasUseCase.listar(currentUserService.getEmpresaId()));
  }
}
