package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.UsuarioCreateRequest;
import ec.sgi.backend.application.dto.UsuarioCreateResult;
import ec.sgi.backend.application.dto.UsuarioEmpresaRequest;
import ec.sgi.backend.application.dto.UsuarioEmpresaPrincipalRequest;
import ec.sgi.backend.application.dto.UsuarioEmpresaDetalleResult;
import ec.sgi.backend.application.dto.UsuarioResult;
import ec.sgi.backend.application.dto.UsuarioUpdateRequest;
import ec.sgi.backend.application.port.in.ActualizarUsuarioCommand;
import ec.sgi.backend.application.port.in.ActualizarUsuarioUseCase;
import ec.sgi.backend.application.port.in.CambiarEmpresaPrincipalUseCase;
import ec.sgi.backend.application.port.in.CrearUsuarioCommand;
import ec.sgi.backend.application.port.in.CrearUsuarioUseCase;
import ec.sgi.backend.application.port.in.EliminarUsuarioUseCase;
import ec.sgi.backend.application.port.in.ListarEmpresasUsuarioUseCase;
import ec.sgi.backend.application.port.in.ListarTodosUsuariosUseCase;
import ec.sgi.backend.application.port.in.ListarUsuariosUseCase;
import ec.sgi.backend.domain.model.UsuarioEmpresa;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
  private final EliminarUsuarioUseCase eliminarUsuarioUseCase;
  private final ListarEmpresasUsuarioUseCase listarEmpresasUsuarioUseCase;
  private final CambiarEmpresaPrincipalUseCase cambiarEmpresaPrincipalUseCase;
  private final ListarTodosUsuariosUseCase listarTodosUsuariosUseCase;
  private final CurrentUserService currentUserService;

  public UsuarioController(
      CrearUsuarioUseCase crearUsuarioUseCase,
      ListarUsuariosUseCase listarUsuariosUseCase,
      ActualizarUsuarioUseCase actualizarUsuarioUseCase,
      EliminarUsuarioUseCase eliminarUsuarioUseCase,
      ListarEmpresasUsuarioUseCase listarEmpresasUsuarioUseCase,
      CambiarEmpresaPrincipalUseCase cambiarEmpresaPrincipalUseCase,
      ListarTodosUsuariosUseCase listarTodosUsuariosUseCase,
      CurrentUserService currentUserService
  ) {
    this.crearUsuarioUseCase = crearUsuarioUseCase;
    this.listarUsuariosUseCase = listarUsuariosUseCase;
    this.actualizarUsuarioUseCase = actualizarUsuarioUseCase;
    this.eliminarUsuarioUseCase = eliminarUsuarioUseCase;
    this.listarEmpresasUsuarioUseCase = listarEmpresasUsuarioUseCase;
    this.cambiarEmpresaPrincipalUseCase = cambiarEmpresaPrincipalUseCase;
    this.listarTodosUsuariosUseCase = listarTodosUsuariosUseCase;
    this.currentUserService = currentUserService;
  }

  @PostMapping
  @Operation(summary = "Crear usuario", description = "Crea un usuario con empresas asignadas.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Usuario creado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado")
  })
  public ResponseEntity<UsuarioCreateResult> crear(@Valid @RequestBody UsuarioCreateRequest request) {
    UsuarioCreateResult result = crearUsuarioUseCase.crear(new CrearUsuarioCommand(
        request.nombre(),
        request.usuario(),
        request.email(),
        request.password(),
        request.roles(),
        toEmpresas(request.empresas()),
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

  @GetMapping("/todos")
  @Operation(summary = "Listar todos los usuarios", description = "Lista todos los usuarios del sistema.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de usuarios"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<UsuarioResult>> listarTodos() {
    return ResponseEntity.ok(listarTodosUsuariosUseCase.listarTodos());
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
    Long empresaId = currentUserService.isAdmin() ? null : currentUserService.getEmpresaId();
    UsuarioResult result = actualizarUsuarioUseCase.actualizar(
        empresaId,
        usuarioId,
        new ActualizarUsuarioCommand(
            request.nombre(),
            request.usuario(),
            request.email(),
            request.roles(),
            toEmpresas(request.empresas()),
            request.activo(),
            request.password()
        )
    );
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{usuarioId}/empresas")
  @Operation(summary = "Listar empresas de usuario", description = "Lista empresas asociadas a un usuario.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de empresas"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
  })
  public ResponseEntity<List<UsuarioEmpresaDetalleResult>> listarEmpresas(
      @Parameter(description = "ID del usuario") @PathVariable Long usuarioId
  ) {
    return ResponseEntity.ok(
        listarEmpresasUsuarioUseCase.listarEmpresas(currentUserService.getEmpresaId(), usuarioId)
    );
  }

  @PutMapping("/{usuarioId}/empresa-principal")
  @Operation(summary = "Cambiar empresa principal", description = "Asigna la empresa principal de un usuario.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
  })
  public ResponseEntity<UsuarioResult> cambiarEmpresaPrincipal(
      @Parameter(description = "ID del usuario") @PathVariable Long usuarioId,
      @Valid @RequestBody UsuarioEmpresaPrincipalRequest request
  ) {
    UsuarioResult result = cambiarEmpresaPrincipalUseCase.cambiar(
        currentUserService.getEmpresaId(),
        usuarioId,
        request.empresaId()
    );
    return ResponseEntity.ok(result);
  }

  @DeleteMapping("/{usuarioId}")
  @Operation(summary = "Eliminar usuario", description = "Elimina un usuario existente.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
  })
  public ResponseEntity<Void> eliminar(
      @Parameter(description = "ID del usuario") @PathVariable Long usuarioId
  ) {
    eliminarUsuarioUseCase.eliminar(currentUserService.getEmpresaId(), usuarioId);
    return ResponseEntity.noContent().build();
  }

  private List<UsuarioEmpresa> toEmpresas(List<UsuarioEmpresaRequest> empresas) {
    if (empresas == null) {
      return List.of();
    }
    return empresas.stream()
        .filter(empresa -> empresa != null && empresa.empresaId() != null)
        .map(empresa -> new UsuarioEmpresa(
            empresa.empresaId(),
            Boolean.TRUE.equals(empresa.principal())
        ))
        .toList();
  }
}
