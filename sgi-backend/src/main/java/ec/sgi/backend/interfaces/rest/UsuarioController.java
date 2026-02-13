package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.UsuarioCreateRequest;
import ec.sgi.backend.application.dto.UsuarioCreateResult;
import ec.sgi.backend.application.dto.UsuarioResult;
import ec.sgi.backend.application.dto.UsuarioUpdateRequest;
import ec.sgi.backend.application.port.in.ActualizarUsuarioCommand;
import ec.sgi.backend.application.port.in.ActualizarUsuarioUseCase;
import ec.sgi.backend.application.port.in.CrearUsuarioCommand;
import ec.sgi.backend.application.port.in.CrearUsuarioUseCase;
import ec.sgi.backend.application.port.in.ListarUsuariosUseCase;
import ec.sgi.backend.security.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestion de usuarios.")
public class UsuarioController {
  private final CrearUsuarioUseCase crearUsuarioUseCase;
  private final ListarUsuariosUseCase listarUsuariosUseCase;
  private final ActualizarUsuarioUseCase actualizarUsuarioUseCase;
  private final CurrentUserService currentUserService;

  public UsuarioController(
      CrearUsuarioUseCase crearUsuarioUseCase,
      ListarUsuariosUseCase listarUsuariosUseCase,
      ActualizarUsuarioUseCase actualizarUsuarioUseCase,
      CurrentUserService currentUserService
  ) {
    this.crearUsuarioUseCase = crearUsuarioUseCase;
    this.listarUsuariosUseCase = listarUsuariosUseCase;
    this.actualizarUsuarioUseCase = actualizarUsuarioUseCase;
    this.currentUserService = currentUserService;
  }

  @PostMapping
  @Operation(summary = "Crear usuario", description = "Crea un usuario para la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Usuario creado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado")
  })
  public ResponseEntity<UsuarioCreateResult> crear(@Valid @RequestBody UsuarioCreateRequest request) {
    Long empresaId = currentUserService.getEmpresaId();
    UsuarioCreateResult result = crearUsuarioUseCase.crear(new CrearUsuarioCommand(
        empresaId,
        request.nombre(),
        request.usuario(),
        request.email(),
        request.password(),
        request.rol(),
        request.activo()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  @Operation(summary = "Listar usuarios", description = "Lista usuarios de la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de usuarios"),
      @ApiResponse(responseCode = "401", description = "No autorizado")
  })
  public ResponseEntity<List<UsuarioResult>> listar() {
    return ResponseEntity.ok(listarUsuariosUseCase.listar(currentUserService.getEmpresaId()));
  }

  @PutMapping("/{usuarioId}")
  @Operation(summary = "Actualizar usuario", description = "Actualiza datos de un usuario.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
  })
  public ResponseEntity<UsuarioResult> actualizar(
      @Parameter(description = "ID del usuario") @PathVariable Long usuarioId,
      @Valid @RequestBody UsuarioUpdateRequest request
  ) {
    UsuarioResult result = actualizarUsuarioUseCase.actualizar(
        currentUserService.getEmpresaId(),
        usuarioId,
        new ActualizarUsuarioCommand(
            request.nombre(),
            request.usuario(),
            request.email(),
            request.rol(),
            request.activo(),
            request.password()
        )
    );
    return ResponseEntity.ok(result);
  }
}
