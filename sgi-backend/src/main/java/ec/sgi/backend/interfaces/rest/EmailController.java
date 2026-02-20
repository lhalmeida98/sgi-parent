package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.EnviarFacturaEmailRequest;
import ec.sgi.backend.application.usecase.EnviarFacturaPorEmailUseCase;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
@Tag(name = "Email", description = "Envio de correos transaccionales.")
public class EmailController {
  private final EnviarFacturaPorEmailUseCase enviarFacturaPorEmailUseCase;
  private final PermisoService permisoService;
  private final CurrentUserService currentUserService;

  public EmailController(
      EnviarFacturaPorEmailUseCase enviarFacturaPorEmailUseCase,
      PermisoService permisoService,
      CurrentUserService currentUserService
  ) {
    this.enviarFacturaPorEmailUseCase = enviarFacturaPorEmailUseCase;
    this.permisoService = permisoService;
    this.currentUserService = currentUserService;
  }

  @PostMapping("/factura")
  @Operation(summary = "Enviar factura por email", description = "Envia la factura (PDF + XML) al cliente.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "202", description = "Solicitud aceptada"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Factura o cliente no encontrados"),
      @ApiResponse(responseCode = "502", description = "Error enviando correo")
  })
  public ResponseEntity<Void> enviarFactura(@Valid @RequestBody EnviarFacturaEmailRequest request) {
    permisoService.requirePermiso("FACTURACION");
    Long empresaId = currentUserService.getEmpresaId();
    enviarFacturaPorEmailUseCase.execute(
        request.facturaId(),
        empresaId,
        request.subject()
    );
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }
}
