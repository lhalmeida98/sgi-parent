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
@RequestMapping("/api/impuestos")
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
  public ResponseEntity<ImpuestoCreateResult> crear(@Valid @RequestBody ImpuestoCreateRequest request) {
    permisoService.requirePermiso(Permisos.IMPUESTO_GESTION);
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
  public ResponseEntity<List<ImpuestoResult>> listar() {
    permisoService.requirePermiso(Permisos.IMPUESTO_GESTION);
    return ResponseEntity.ok(listarImpuestosUseCase.listar(currentUserService.getEmpresaId()));
  }

  @PutMapping("/{impuestoId}")
  public ResponseEntity<ImpuestoResult> actualizar(
      @PathVariable Long impuestoId,
      @Valid @RequestBody ImpuestoUpdateRequest request
  ) {
    permisoService.requirePermiso(Permisos.IMPUESTO_GESTION);
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
