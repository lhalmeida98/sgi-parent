package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.CategoriaCreateRequest;
import ec.sgi.backend.application.dto.CategoriaCreateResult;
import ec.sgi.backend.application.dto.CategoriaResult;
import ec.sgi.backend.application.dto.CategoriaUpdateRequest;
import ec.sgi.backend.application.port.in.ActualizarCategoriaCommand;
import ec.sgi.backend.application.port.in.ActualizarCategoriaUseCase;
import ec.sgi.backend.application.port.in.CrearCategoriaCommand;
import ec.sgi.backend.application.port.in.CrearCategoriaUseCase;
import ec.sgi.backend.application.port.in.ListarCategoriasUseCase;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
import ec.sgi.backend.security.Permisos;
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
@RequestMapping("/api/categorias")
public class CategoriaController {
  private final CrearCategoriaUseCase crearCategoriaUseCase;
  private final ListarCategoriasUseCase listarCategoriasUseCase;
  private final ActualizarCategoriaUseCase actualizarCategoriaUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public CategoriaController(
      CrearCategoriaUseCase crearCategoriaUseCase,
      ListarCategoriasUseCase listarCategoriasUseCase,
      ActualizarCategoriaUseCase actualizarCategoriaUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.crearCategoriaUseCase = crearCategoriaUseCase;
    this.listarCategoriasUseCase = listarCategoriasUseCase;
    this.actualizarCategoriaUseCase = actualizarCategoriaUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @PostMapping
  public ResponseEntity<CategoriaCreateResult> crear(@Valid @RequestBody CategoriaCreateRequest request) {
    permisoService.requirePermiso(Permisos.CATEGORIA_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    CategoriaCreateResult result = crearCategoriaUseCase.crear(new CrearCategoriaCommand(
        empresaId,
        request.nombre(),
        request.descripcion()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  public ResponseEntity<List<CategoriaResult>> listar() {
    permisoService.requirePermiso(Permisos.CATEGORIA_GESTION);
    return ResponseEntity.ok(listarCategoriasUseCase.listar(currentUserService.getEmpresaId()));
  }

  @PutMapping("/{categoriaId}")
  public ResponseEntity<CategoriaResult> actualizar(
      @PathVariable Long categoriaId,
      @Valid @RequestBody CategoriaUpdateRequest request
  ) {
    permisoService.requirePermiso(Permisos.CATEGORIA_GESTION);
    CategoriaResult result = actualizarCategoriaUseCase.actualizar(
        currentUserService.getEmpresaId(),
        categoriaId,
        new ActualizarCategoriaCommand(
        request.nombre(),
        request.descripcion()
    ));
    return ResponseEntity.ok(result);
  }
}
