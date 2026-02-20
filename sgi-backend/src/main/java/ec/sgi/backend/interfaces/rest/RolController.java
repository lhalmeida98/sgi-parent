package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.AccionResult;
import ec.sgi.backend.application.dto.RolCreateRequest;
import ec.sgi.backend.application.dto.RolCreateResult;
import ec.sgi.backend.application.dto.RolResult;
import ec.sgi.backend.application.dto.RolUpdateRequest;
import ec.sgi.backend.application.port.in.ActualizarRolCommand;
import ec.sgi.backend.application.port.in.ActualizarRolUseCase;
import ec.sgi.backend.application.port.in.CrearRolCommand;
import ec.sgi.backend.application.port.in.CrearRolUseCase;
import ec.sgi.backend.application.port.in.EliminarRolUseCase;
import ec.sgi.backend.application.port.in.ListarAccionesUseCase;
import ec.sgi.backend.application.port.in.ListarRolesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Gestion de roles y permisos.")
public class RolController {
  private final CrearRolUseCase crearRolUseCase;
  private final ActualizarRolUseCase actualizarRolUseCase;
  private final EliminarRolUseCase eliminarRolUseCase;
  private final ListarRolesUseCase listarRolesUseCase;
  private final ListarAccionesUseCase listarAccionesUseCase;

  public RolController(
      CrearRolUseCase crearRolUseCase,
      ActualizarRolUseCase actualizarRolUseCase,
      EliminarRolUseCase eliminarRolUseCase,
      ListarRolesUseCase listarRolesUseCase,
      ListarAccionesUseCase listarAccionesUseCase
  ) {
    this.crearRolUseCase = crearRolUseCase;
    this.actualizarRolUseCase = actualizarRolUseCase;
    this.eliminarRolUseCase = eliminarRolUseCase;
    this.listarRolesUseCase = listarRolesUseCase;
    this.listarAccionesUseCase = listarAccionesUseCase;
  }

  @PostMapping
  @Operation(summary = "Crear rol", description = "Crea un rol global.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Rol creado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado")
  })
  public ResponseEntity<RolCreateResult> crear(@Valid @RequestBody RolCreateRequest request) {
    RolCreateResult result = crearRolUseCase.crear(new CrearRolCommand(
        request.nombre(),
        request.descripcion(),
        request.accionesIds(),
        request.activo()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  @Operation(summary = "Listar roles", description = "Lista roles globales.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de roles"),
      @ApiResponse(responseCode = "401", description = "No autorizado")
  })
  public ResponseEntity<List<RolResult>> listar() {
    return ResponseEntity.ok(listarRolesUseCase.listar());
  }

  @GetMapping("/acciones")
  @Operation(summary = "Listar acciones", description = "Lista acciones disponibles para asignar a roles.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de acciones"),
      @ApiResponse(responseCode = "401", description = "No autorizado")
  })
  public ResponseEntity<List<AccionResult>> listarAcciones() {
    return ResponseEntity.ok(listarAccionesUseCase.listar());
  }

  @PutMapping("/{rolId}")
  @Operation(summary = "Actualizar rol", description = "Actualiza un rol existente.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Rol actualizado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Rol no encontrado")
  })
  public ResponseEntity<RolResult> actualizar(
      @PathVariable Long rolId,
      @Valid @RequestBody RolUpdateRequest request
  ) {
    RolResult result = actualizarRolUseCase.actualizar(rolId, new ActualizarRolCommand(
        request.nombre(),
        request.descripcion(),
        request.accionesIds(),
        request.activo()
    ));
    return ResponseEntity.ok(result);
  }

  @DeleteMapping("/{rolId}")
  @Operation(summary = "Eliminar rol", description = "Elimina un rol existente.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Rol eliminado"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Rol no encontrado")
  })
  public ResponseEntity<Void> eliminar(@PathVariable Long rolId) {
    eliminarRolUseCase.eliminar(rolId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/permisos")
  @Operation(summary = "Listar permisos", description = "Lista todos los permisos disponibles en el sistema.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de permisos"),
      @ApiResponse(responseCode = "401", description = "No autorizado")
  })
  public ResponseEntity<List<String>> listarPermisos() {
    List<String> permisos = listarAccionesUseCase.listar().stream()
        .filter(accion -> accion != null && accion.activo())
        .map(AccionResult::codigo)
        .filter(codigo -> codigo != null && !codigo.isBlank())
        .distinct()
        .toList();
    return ResponseEntity.ok(permisos);
  }
}
