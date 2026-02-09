package ec.sgi.backend.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Util", description = "Endpoints publicos de utilidad y diagnostico.")
public class PingController {
  private final String fromEmail;
  private final String fromName;

  public PingController(
      @Value("${mail.from.email:}") String fromEmail,
      @Value("${mail.from.name:}") String fromName
  ) {
    this.fromEmail = fromEmail;
    this.fromName = fromName;
  }

  @GetMapping("/ping")
  @Operation(summary = "Ping", description = "Verifica que el servicio este en linea.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Servicio disponible")
  })
  public ResponseEntity<Map<String, String>> ping() {
    return ResponseEntity.ok(Map.of("status", "ok"));
  }

  @GetMapping("/email/config")
  @Operation(summary = "Config correo", description = "Muestra configuracion efectiva del proveedor de correo.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Configuracion disponible")
  })
  public ResponseEntity<Map<String, Object>> mailHealth() {
    boolean resendDefault = fromEmail != null && fromEmail.toLowerCase().endsWith("@resend.dev");
    return ResponseEntity.ok(Map.of(
        "status", "ok",
        "provider", "resend",
        "fromEmail", fromEmail == null ? "" : fromEmail,
        "fromName", fromName == null ? "" : fromName,
        "restrictedToAccountEmail", resendDefault
    ));
  }
}
