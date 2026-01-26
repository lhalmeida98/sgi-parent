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
  public ResponseEntity<UsuarioCreateResult> crear(@Valid @RequestBody UsuarioCreateRequest request) {
    Long empresaId = currentUserService.getEmpresaId();
    UsuarioCreateResult result = crearUsuarioUseCase.crear(new CrearUsuarioCommand(
        empresaId,
        request.nombre(),
        request.email(),
        request.password(),
        request.rol(),
        request.activo()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  public ResponseEntity<List<UsuarioResult>> listar() {
    return ResponseEntity.ok(listarUsuariosUseCase.listar(currentUserService.getEmpresaId()));
  }

  @PutMapping("/{usuarioId}")
  public ResponseEntity<UsuarioResult> actualizar(
      @PathVariable Long usuarioId,
      @Valid @RequestBody UsuarioUpdateRequest request
  ) {
    UsuarioResult result = actualizarUsuarioUseCase.actualizar(
        currentUserService.getEmpresaId(),
        usuarioId,
        new ActualizarUsuarioCommand(
            request.nombre(),
            request.email(),
            request.rol(),
            request.activo(),
            request.password()
        )
    );
    return ResponseEntity.ok(result);
  }
}
