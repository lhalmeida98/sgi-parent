package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.DocumentoClienteResult;
import ec.sgi.backend.application.dto.DocumentoClienteEstadoRequest;
import ec.sgi.backend.application.port.in.AnularDocumentoClienteCommand;
import ec.sgi.backend.application.port.in.AnularDocumentoClienteUseCase;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.port.in.ListarDocumentosClienteUseCase;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Documentos Cliente", description = "Documentos emitidos a clientes (facturas SRI).")
public class DocumentoClienteController {
  private final ListarDocumentosClienteUseCase listarDocumentosClienteUseCase;
  private final AnularDocumentoClienteUseCase anularDocumentoClienteUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public DocumentoClienteController(
      ListarDocumentosClienteUseCase listarDocumentosClienteUseCase,
      AnularDocumentoClienteUseCase anularDocumentoClienteUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.listarDocumentosClienteUseCase = listarDocumentosClienteUseCase;
    this.anularDocumentoClienteUseCase = anularDocumentoClienteUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @GetMapping("/documentos-cliente")
  @Operation(summary = "Listar documentos", description = "Lista documentos de clientes. Puede filtrar por clienteId.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de documentos"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<DocumentoClienteResult>> listar(
      @Parameter(description = "ID del cliente") @RequestParam(required = false) Long clienteId
  ) {
    permisoService.requirePermiso("CLIENTES");
    Long empresaId = currentUserService.getEmpresaId();
    return ResponseEntity.ok(listarDocumentosClienteUseCase.listar(empresaId, clienteId));
  }

  @GetMapping("/clientes/{clienteId}/documentos")
  @Operation(summary = "Listar documentos por cliente", description = "Lista documentos de un cliente.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de documentos"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
  })
  public ResponseEntity<List<DocumentoClienteResult>> listarPorCliente(
      @Parameter(description = "ID del cliente") @PathVariable Long clienteId
  ) {
    permisoService.requirePermiso("CLIENTES");
    Long empresaId = currentUserService.getEmpresaId();
    return ResponseEntity.ok(listarDocumentosClienteUseCase.listar(empresaId, clienteId));
  }

  @PatchMapping("/documentos-cliente/{documentoId}/estado")
  @Operation(summary = "Anular documento", description = "Anula un documento de cliente y bloquea cobros.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Documento anulado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Documento no encontrado")
  })
  public ResponseEntity<DocumentoClienteResult> actualizarEstado(
      @Parameter(description = "ID del documento") @PathVariable Long documentoId,
      @Valid @RequestBody DocumentoClienteEstadoRequest request
  ) {
    permisoService.requirePermiso("CLIENTES");
    Long empresaId = currentUserService.getEmpresaId();
    if (!"ANULADA".equalsIgnoreCase(request.estado())) {
      throw new BusinessRuleException("Estado invalido");
    }
    DocumentoClienteResult result = anularDocumentoClienteUseCase.anular(new AnularDocumentoClienteCommand(
        empresaId,
        documentoId,
        request.motivo()
    ));
    return ResponseEntity.ok(result);
  }
}
