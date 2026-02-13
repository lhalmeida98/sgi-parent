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
import ec.sgi.backend.security.Permisos;
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
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Gestion de roles y permisos.")
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
  @Operation(summary = "Crear rol", description = "Crea un rol para la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Rol creado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado")
  })
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
  @Operation(summary = "Listar roles", description = "Lista roles de la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de roles"),
      @ApiResponse(responseCode = "401", description = "No autorizado")
  })
  public ResponseEntity<List<RolResult>> listar() {
    return ResponseEntity.ok(listarRolesUseCase.listar(currentUserService.getEmpresaId()));
  }

  @GetMapping("/acciones")
  @Operation(summary = "Listar acciones", description = "Lista acciones disponibles para asignar a roles.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de acciones"),
      @ApiResponse(responseCode = "401", description = "No autorizado")
  })
  public ResponseEntity<List<AccionResult>> listarAcciones() {
    return ResponseEntity.ok(listarAccionesUseCase.listar(currentUserService.getEmpresaId()));
  }

  @GetMapping("/permisos")
  @Operation(summary = "Listar permisos", description = "Lista todos los permisos disponibles en el sistema.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de permisos"),
      @ApiResponse(responseCode = "401", description = "No autorizado")
  })
  public ResponseEntity<List<String>> listarPermisos() {
    return ResponseEntity.ok(Permisos.TODOS);
  }
}
