package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.EmpresaCreateRequest;
import ec.sgi.backend.application.dto.EmpresaCreateResult;
import ec.sgi.backend.application.dto.EmpresaResult;
import ec.sgi.backend.application.dto.EmpresaUpdateRequest;
import ec.sgi.backend.application.dto.FirmaElectronicaResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.port.in.ActualizarEmpresaCommand;
import ec.sgi.backend.application.port.in.ActualizarEmpresaUseCase;
import ec.sgi.backend.application.port.in.CrearEmpresaCommand;
import ec.sgi.backend.application.port.in.CrearEmpresaUseCase;
import ec.sgi.backend.application.port.in.ListarEmpresasUseCase;
import ec.sgi.backend.application.port.in.SubirFirmaElectronicaCommand;
import ec.sgi.backend.application.port.in.SubirFirmaElectronicaUseCase;
import ec.sgi.backend.application.port.in.SubirLogoEmpresaCommand;
import ec.sgi.backend.application.port.in.SubirLogoEmpresaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/empresas")
@Tag(name = "Empresas", description = "Gestion de empresas.")
public class EmpresaController {
  private final CrearEmpresaUseCase crearEmpresaUseCase;
  private final ListarEmpresasUseCase listarEmpresasUseCase;
  private final SubirFirmaElectronicaUseCase subirFirmaElectronicaUseCase;
  private final ActualizarEmpresaUseCase actualizarEmpresaUseCase;
  private final SubirLogoEmpresaUseCase subirLogoEmpresaUseCase;

  public EmpresaController(
      CrearEmpresaUseCase crearEmpresaUseCase,
      ListarEmpresasUseCase listarEmpresasUseCase,
      SubirFirmaElectronicaUseCase subirFirmaElectronicaUseCase,
      ActualizarEmpresaUseCase actualizarEmpresaUseCase,
      SubirLogoEmpresaUseCase subirLogoEmpresaUseCase
  ) {
    this.crearEmpresaUseCase = crearEmpresaUseCase;
    this.listarEmpresasUseCase = listarEmpresasUseCase;
    this.subirFirmaElectronicaUseCase = subirFirmaElectronicaUseCase;
    this.actualizarEmpresaUseCase = actualizarEmpresaUseCase;
    this.subirLogoEmpresaUseCase = subirLogoEmpresaUseCase;
  }

  @PostMapping
  @Operation(summary = "Crear empresa", description = "Crea una empresa.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Empresa creada"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<EmpresaCreateResult> crear(@Valid @RequestBody EmpresaCreateRequest request) {
    EmpresaCreateResult result = crearEmpresaUseCase.crear(new CrearEmpresaCommand(
        request.ambiente(),
        request.tipoEmision(),
        request.razonSocial(),
        request.nombreComercial(),
        request.ruc(),
        request.dirMatriz(),
        request.estab(),
        request.ptoEmi(),
        request.secuencial(),
        Boolean.TRUE.equals(request.obligadoContabilidad()),
        Boolean.TRUE.equals(request.regimenRimpe()),
        request.creditoDiasDefault()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  @Operation(summary = "Listar empresas", description = "Lista empresas registradas.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de empresas"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<EmpresaResult>> listar() {
    return ResponseEntity.ok(listarEmpresasUseCase.listar());
  }

  @PutMapping("/{empresaId}")
  @Operation(summary = "Actualizar empresa", description = "Actualiza datos de una empresa.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Empresa actualizada"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
  })
  public ResponseEntity<EmpresaResult> actualizar(
      @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
      @Valid @RequestBody EmpresaUpdateRequest request
  ) {
    EmpresaResult result = actualizarEmpresaUseCase.actualizar(empresaId, new ActualizarEmpresaCommand(
        request.ambiente(),
        request.tipoEmision(),
        request.razonSocial(),
        request.nombreComercial(),
        request.dirMatriz(),
        request.estab(),
        request.ptoEmi(),
        request.secuencial(),
        Boolean.TRUE.equals(request.obligadoContabilidad()),
        Boolean.TRUE.equals(request.regimenRimpe()),
        request.creditoDiasDefault()
    ));
    return ResponseEntity.ok(result);
  }

  @PostMapping(path = "/{empresaId}/firma", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Subir firma electronica", description = "Sube el archivo P12/PFX de firma.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Firma subida"),
      @ApiResponse(responseCode = "400", description = "Archivo invalido"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
  })
  public ResponseEntity<FirmaElectronicaResult> subirFirma(
      @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
      @Parameter(description = "Archivo P12/PFX") @RequestParam("archivo") MultipartFile archivo,
      @Parameter(description = "Clave del archivo de firma") @RequestParam("clave") String clave
  ) {
    String nombreArchivo = archivo.getOriginalFilename();
    if (nombreArchivo == null) {
      nombreArchivo = "";
    }
    byte[] contenido;
    try {
      contenido = archivo.getBytes();
    } catch (Exception ex) {
      throw new BusinessRuleException("No se pudo leer el archivo de firma");
    }
    FirmaElectronicaResult result = subirFirmaElectronicaUseCase.subir(new SubirFirmaElectronicaCommand(
        empresaId,
        nombreArchivo,
        archivo.getContentType(),
        contenido,
        clave
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @PostMapping(path = "/{empresaId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Subir logo", description = "Sube el logo de la empresa.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Logo subido"),
      @ApiResponse(responseCode = "400", description = "Archivo invalido"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
  })
  public ResponseEntity<EmpresaResult> subirLogo(
      @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
      @Parameter(description = "Archivo de imagen") @RequestParam("archivo") MultipartFile archivo
  ) {
    String nombreArchivo = archivo.getOriginalFilename();
    if (nombreArchivo == null) {
      nombreArchivo = "";
    }
    byte[] contenido;
    try {
      contenido = archivo.getBytes();
    } catch (Exception ex) {
      throw new BusinessRuleException("No se pudo leer el archivo de logo");
    }
    EmpresaResult result = subirLogoEmpresaUseCase.subir(new SubirLogoEmpresaCommand(
        empresaId,
        nombreArchivo,
        archivo.getContentType(),
        contenido
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }
}
