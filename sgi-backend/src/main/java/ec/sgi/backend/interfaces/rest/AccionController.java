package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.AccionCreateRequest;
import ec.sgi.backend.application.dto.AccionCreateResult;
import ec.sgi.backend.application.dto.AccionResult;
import ec.sgi.backend.application.port.in.CrearAccionCommand;
import ec.sgi.backend.application.port.in.CrearAccionUseCase;
import ec.sgi.backend.application.port.in.ListarAccionesUseCase;
import ec.sgi.backend.security.CurrentUserService;
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
@RequestMapping("/api/acciones")
public class AccionController {
  private final CrearAccionUseCase crearAccionUseCase;
  private final ListarAccionesUseCase listarAccionesUseCase;
  private final CurrentUserService currentUserService;

  public AccionController(
      CrearAccionUseCase crearAccionUseCase,
      ListarAccionesUseCase listarAccionesUseCase,
      CurrentUserService currentUserService
  ) {
    this.crearAccionUseCase = crearAccionUseCase;
    this.listarAccionesUseCase = listarAccionesUseCase;
    this.currentUserService = currentUserService;
  }

  @PostMapping
  public ResponseEntity<AccionCreateResult> crear(@Valid @RequestBody AccionCreateRequest request) {
    Long empresaId = currentUserService.getEmpresaId();
    AccionCreateResult result = crearAccionUseCase.crear(new CrearAccionCommand(
        empresaId,
        request.codigo(),
        request.descripcion(),
        request.activo()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  public ResponseEntity<List<AccionResult>> listar() {
    return ResponseEntity.ok(listarAccionesUseCase.listar(currentUserService.getEmpresaId()));
  }
}
