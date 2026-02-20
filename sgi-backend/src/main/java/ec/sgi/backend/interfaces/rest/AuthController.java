package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.AccionMenuResult;
import ec.sgi.backend.application.dto.AuthLoginRequest;
import ec.sgi.backend.application.dto.AuthLoginResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.port.out.AccionRepository;
import ec.sgi.backend.application.port.out.RolRepository;
import ec.sgi.backend.domain.model.Accion;
import ec.sgi.backend.security.JwtService;
import ec.sgi.backend.security.UsuarioPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticacion y emision de tokens.")
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final RolRepository rolRepository;
  private final AccionRepository accionRepository;

  public AuthController(
      AuthenticationManager authenticationManager,
      JwtService jwtService,
      RolRepository rolRepository,
      AccionRepository accionRepository
  ) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.rolRepository = rolRepository;
    this.accionRepository = accionRepository;
  }

  @PostMapping("/login")
  @Operation(summary = "Login", description = "Autentica un usuario y devuelve un JWT.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Autenticado"),
      @ApiResponse(responseCode = "400", description = "Credenciales invalidas"),
      @ApiResponse(responseCode = "401", description = "No autorizado")
  })
  public ResponseEntity<AuthLoginResult> login(@Valid @RequestBody AuthLoginRequest request) {
    String login = request.usuario();
    if (login == null || login.isBlank()) {
      login = request.email();
    }
    if (login == null || login.isBlank()) {
      throw new BusinessRuleException("Usuario o email requerido");
    }
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(login, request.password())
    );
    UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
    String token = jwtService.generateToken(principal);
    List<AccionMenuResult> acciones = resolveAcciones(principal.getRoles());
    AuthLoginResult result = new AuthLoginResult(
        token,
        "Bearer",
        principal.getRoles(),
        acciones,
        principal.getEmpresaId()
    );
    return ResponseEntity.ok(result);
  }

  private List<AccionMenuResult> resolveAcciones(List<String> roles) {
    if (roles == null || roles.isEmpty()) {
      return List.of();
    }
    boolean esAdmin = roles.stream().anyMatch(rol -> rol != null && rol.equalsIgnoreCase("ADMIN"));
    List<Accion> acciones = esAdmin
        ? accionRepository.findAll()
        : accionRepository.findByCodigoIn(rolRepository.findPermisosByRoles(roles));
    Set<String> claves = new LinkedHashSet<>();
    List<AccionMenuResult> resultado = new ArrayList<>();
    for (Accion accion : acciones) {
      if (accion == null || !accion.activo()) {
        continue;
      }
      String url = accion.url();
      String clave = (url != null && !url.isBlank()) ? url : accion.codigo();
      if (clave == null || clave.isBlank() || !claves.add(clave)) {
        continue;
      }
      resultado.add(new AccionMenuResult(
          accion.nombre(),
          accion.descripcion(),
          url,
          accion.icono(),
          accion.tipo()
      ));
    }
    return resultado;
  }
}
