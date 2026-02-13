package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.PreordenCreateRequest;
import ec.sgi.backend.application.dto.PreordenCreateResult;
import ec.sgi.backend.application.dto.PreordenItemRequest;
import ec.sgi.backend.application.dto.PreordenResult;
import ec.sgi.backend.application.port.in.CrearPreordenCommand;
import ec.sgi.backend.application.port.in.CrearPreordenUseCase;
import ec.sgi.backend.application.port.in.ListarPreordenesUseCase;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
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
@RequestMapping("/api/preordenes")
@Tag(name = "Preordenes", description = "Gestion de preordenes.")
public class PreordenController {
  private final CrearPreordenUseCase crearPreordenUseCase;
  private final ListarPreordenesUseCase listarPreordenesUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public PreordenController(
      CrearPreordenUseCase crearPreordenUseCase,
      ListarPreordenesUseCase listarPreordenesUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.crearPreordenUseCase = crearPreordenUseCase;
    this.listarPreordenesUseCase = listarPreordenesUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @PostMapping
  @Operation(summary = "Crear preorden", description = "Crea una preorden para la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Preorden creada"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<PreordenCreateResult> crear(@Valid @RequestBody PreordenCreateRequest request) {
    permisoService.requirePermiso(Permisos.PREORDEN_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    PreordenCreateResult result = crearPreordenUseCase.crear(new CrearPreordenCommand(
        empresaId,
        request.clienteId(),
        request.dirEstablecimiento(),
        request.moneda(),
        request.observaciones(),
        request.reservaInventario(),
        request.items().stream()
            .map(this::toItemCommand)
            .toList()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  @Operation(summary = "Listar preordenes", description = "Lista preordenes de la empresa actual.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de preordenes"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<PreordenResult>> listar() {
    permisoService.requirePermiso(Permisos.PREORDEN_GESTION);
    return ResponseEntity.ok(listarPreordenesUseCase.listar(currentUserService.getEmpresaId()));
  }

  private CrearPreordenCommand.ItemPreordenCommand toItemCommand(PreordenItemRequest item) {
    return new CrearPreordenCommand.ItemPreordenCommand(
        item.bodegaId(),
        item.productoId(),
        item.cantidad(),
        item.descuento()
    );
  }
}
