package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.RolCreateRequest;
import ec.sgi.backend.application.dto.RolCreateResult;
import ec.sgi.backend.application.dto.RolResult;
import ec.sgi.backend.application.port.in.CrearRolCommand;
import ec.sgi.backend.application.port.in.CrearRolUseCase;
import ec.sgi.backend.application.port.in.ListarAccionesUseCase;
import ec.sgi.backend.application.port.in.ListarRolesUseCase;
import ec.sgi.backend.application.dto.AccionResult;
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
@RequestMapping("/api/roles")
public class RolController {
  private final CrearRolUseCase crearRolUseCase;
  private final ListarRolesUseCase listarRolesUseCase;
  private final ListarAccionesUseCase listarAccionesUseCase;
  private final CurrentUserService currentUserService;

  public RolController(
      CrearRolUseCase crearRolUseCase,
      ListarRolesUseCase listarRolesUseCase,
      ListarAccionesUseCase listarAccionesUseCase,
      CurrentUserService currentUserService
  ) {
    this.crearRolUseCase = crearRolUseCase;
    this.listarRolesUseCase = listarRolesUseCase;
    this.listarAccionesUseCase = listarAccionesUseCase;
    this.currentUserService = currentUserService;
  }

  @PostMapping
  public ResponseEntity<RolCreateResult> crear(@Valid @RequestBody RolCreateRequest request) {
    Long empresaId = currentUserService.getEmpresaId();
    RolCreateResult result = crearRolUseCase.crear(new CrearRolCommand(
        empresaId,
        request.nombre(),
        request.descripcion(),
        request.permisos()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  public ResponseEntity<List<RolResult>> listar() {
    return ResponseEntity.ok(listarRolesUseCase.listar(currentUserService.getEmpresaId()));
  }

  @GetMapping("/acciones")
  public ResponseEntity<List<AccionResult>> listarAcciones() {
    return ResponseEntity.ok(listarAccionesUseCase.listar(currentUserService.getEmpresaId()));
  }
}
