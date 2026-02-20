package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.AccionCreateRequest;
import ec.sgi.backend.application.dto.AccionCreateResult;
import ec.sgi.backend.application.dto.AccionResult;
import ec.sgi.backend.application.dto.AccionUpdateRequest;
import ec.sgi.backend.application.port.in.ActualizarAccionCommand;
import ec.sgi.backend.application.port.in.ActualizarAccionUseCase;
import ec.sgi.backend.application.port.in.CrearAccionCommand;
import ec.sgi.backend.application.port.in.CrearAccionUseCase;
import ec.sgi.backend.application.port.in.EliminarAccionUseCase;
import ec.sgi.backend.application.port.in.ListarAccionesUseCase;
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
@RequestMapping("/api/acciones")
@Tag(name = "Acciones", description = "Gestion de acciones/permiso.")
public class AccionController {
  private final CrearAccionUseCase crearAccionUseCase;
  private final ActualizarAccionUseCase actualizarAccionUseCase;
  private final EliminarAccionUseCase eliminarAccionUseCase;
  private final ListarAccionesUseCase listarAccionesUseCase;

  public AccionController(
      CrearAccionUseCase crearAccionUseCase,
      ActualizarAccionUseCase actualizarAccionUseCase,
      EliminarAccionUseCase eliminarAccionUseCase,
      ListarAccionesUseCase listarAccionesUseCase
  ) {
    this.crearAccionUseCase = crearAccionUseCase;
    this.actualizarAccionUseCase = actualizarAccionUseCase;
    this.eliminarAccionUseCase = eliminarAccionUseCase;
    this.listarAccionesUseCase = listarAccionesUseCase;
  }

  @PostMapping
  @Operation(summary = "Crear accion", description = "Crea una accion disponible para permisos.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Accion creada"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<AccionCreateResult> crear(@Valid @RequestBody AccionCreateRequest request) {
    AccionCreateResult result = crearAccionUseCase.crear(new CrearAccionCommand(
        request.nombre(),
        request.codigo(),
        request.descripcion(),
        request.url(),
        request.icono(),
        request.tipo(),
        request.activo()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  @Operation(summary = "Listar acciones", description = "Lista acciones disponibles en el sistema.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de acciones"),
      @ApiResponse(responseCode = "401", description = "No autorizado")
  })
  public ResponseEntity<List<AccionResult>> listar() {
    return ResponseEntity.ok(listarAccionesUseCase.listar());
  }

  @PutMapping("/{accionId}")
  @Operation(summary = "Actualizar accion", description = "Actualiza una accion existente.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Accion actualizada"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Accion no encontrada")
  })
  public ResponseEntity<AccionResult> actualizar(
      @PathVariable Long accionId,
      @Valid @RequestBody AccionUpdateRequest request
  ) {
    AccionResult result = actualizarAccionUseCase.actualizar(accionId, new ActualizarAccionCommand(
        request.nombre(),
        request.codigo(),
        request.descripcion(),
        request.url(),
        request.icono(),
        request.tipo(),
        request.activo()
    ));
    return ResponseEntity.ok(result);
  }

  @DeleteMapping("/{accionId}")
  @Operation(summary = "Eliminar accion", description = "Elimina una accion existente.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Accion eliminada"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Accion no encontrada")
  })
  public ResponseEntity<Void> eliminar(@PathVariable Long accionId) {
    eliminarAccionUseCase.eliminar(accionId);
    return ResponseEntity.noContent().build();
  }
}
