package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.ClienteCreateRequest;
import ec.sgi.backend.application.dto.ClienteCreateResult;
import ec.sgi.backend.application.dto.ClienteResult;
import ec.sgi.backend.application.port.in.CrearClienteCommand;
import ec.sgi.backend.application.port.in.CrearClienteUseCase;
import ec.sgi.backend.application.port.in.ListarClientesUseCase;
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
@RequestMapping("/api/clientes")
public class ClienteController {
  private final CrearClienteUseCase crearClienteUseCase;
  private final ListarClientesUseCase listarClientesUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public ClienteController(
      CrearClienteUseCase crearClienteUseCase,
      ListarClientesUseCase listarClientesUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.crearClienteUseCase = crearClienteUseCase;
    this.listarClientesUseCase = listarClientesUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @PostMapping
  public ResponseEntity<ClienteCreateResult> crear(@Valid @RequestBody ClienteCreateRequest request) {
    permisoService.requirePermiso(Permisos.CLIENTE_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    ClienteCreateResult result = crearClienteUseCase.crear(new CrearClienteCommand(
        empresaId,
        request.tipoIdentificacion(),
        request.identificacion(),
        request.razonSocial(),
        request.email(),
        request.direccion()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  public ResponseEntity<List<ClienteResult>> listar() {
    permisoService.requirePermiso(Permisos.CLIENTE_GESTION);
    return ResponseEntity.ok(listarClientesUseCase.listar(currentUserService.getEmpresaId()));
  }
}
