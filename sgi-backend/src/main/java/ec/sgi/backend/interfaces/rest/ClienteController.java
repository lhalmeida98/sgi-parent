package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.ClienteCreateRequest;
import ec.sgi.backend.application.dto.ClienteCreateResult;
import ec.sgi.backend.application.dto.ClienteResult;
import ec.sgi.backend.application.port.in.CrearClienteCommand;
import ec.sgi.backend.application.port.in.CrearClienteUseCase;
import ec.sgi.backend.application.port.in.ListarClientesUseCase;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
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
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Gestion de clientes.")
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
  @Operation(summary = "Crear cliente", description = "Crea un cliente para la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Cliente creado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<ClienteCreateResult> crear(@Valid @RequestBody ClienteCreateRequest request) {
    permisoService.requirePermiso("CLIENTES");
    Long empresaId = currentUserService.getEmpresaId();
    ClienteCreateResult result = crearClienteUseCase.crear(new CrearClienteCommand(
        empresaId,
        request.tipoIdentificacion(),
        request.identificacion(),
        request.razonSocial(),
        request.email(),
        request.direccion(),
        request.creditoDias()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  @Operation(summary = "Listar clientes", description = "Lista clientes de la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de clientes"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<ClienteResult>> listar() {
    permisoService.requirePermiso("CLIENTES");
    return ResponseEntity.ok(listarClientesUseCase.listar(currentUserService.getEmpresaId()));
  }
}
