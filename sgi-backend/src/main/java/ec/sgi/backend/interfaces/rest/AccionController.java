package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.AccionCreateRequest;
import ec.sgi.backend.application.dto.AccionCreateResult;
import ec.sgi.backend.application.dto.AccionResult;
import ec.sgi.backend.application.port.in.CrearAccionCommand;
import ec.sgi.backend.application.port.in.CrearAccionUseCase;
import ec.sgi.backend.application.port.in.ListarAccionesUseCase;
import ec.sgi.backend.security.CurrentUserService;
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
@RequestMapping("/api/acciones")
@Tag(name = "Acciones", description = "Gestion de acciones/permiso.")
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
  @Operation(summary = "Crear accion", description = "Crea una accion disponible para permisos.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Accion creada"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
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
  @Operation(summary = "Listar acciones", description = "Lista acciones disponibles para la empresa.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de acciones"),
      @ApiResponse(responseCode = "401", description = "No autorizado")
  })
  public ResponseEntity<List<AccionResult>> listar() {
    return ResponseEntity.ok(listarAccionesUseCase.listar(currentUserService.getEmpresaId()));
  }
}
