package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.FacturaCreateRequest;
import ec.sgi.backend.application.dto.FacturaCreateResult;
import ec.sgi.backend.application.dto.FacturaEstadoResult;
import ec.sgi.backend.application.dto.FacturaProcesoResult;
import ec.sgi.backend.application.dto.FacturaResumenPageResult;
import ec.sgi.backend.application.mapper.FacturaCommandMapper;
import ec.sgi.backend.application.port.in.ConsultarEstadoFacturaCommand;
import ec.sgi.backend.application.port.in.ConsultarEstadoFacturaUseCase;
import ec.sgi.backend.application.port.in.ConsultarFacturaEnProcesoUseCase;
import ec.sgi.backend.application.port.in.CrearFacturaUseCase;
import ec.sgi.backend.application.port.in.GenerarFacturaPdfCommand;
import ec.sgi.backend.application.port.in.GenerarFacturaPdfUseCase;
import ec.sgi.backend.application.port.in.ListarFacturasEnProcesoUseCase;
import ec.sgi.backend.application.port.in.ListarFacturasUseCase;
import ec.sgi.backend.application.port.in.ObtenerFacturaXmlUseCase;
import ec.sgi.backend.application.port.in.ReenviarFacturasEnProcesoUseCase;
import ec.sgi.backend.application.port.in.ReenviarFacturaEnProcesoUseCase;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
import ec.sgi.backend.security.Permisos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/facturas")
@Tag(name = "Facturas", description = "Emision y consulta de facturas.")
public class FacturaController {
  private final CrearFacturaUseCase crearFacturaUseCase;
  private final ConsultarEstadoFacturaUseCase consultarEstadoFacturaUseCase;
  private final ConsultarFacturaEnProcesoUseCase consultarFacturaEnProcesoUseCase;
  private final ListarFacturasEnProcesoUseCase listarFacturasEnProcesoUseCase;
  private final ReenviarFacturasEnProcesoUseCase reenviarFacturasEnProcesoUseCase;
  private final ReenviarFacturaEnProcesoUseCase reenviarFacturaEnProcesoUseCase;
  private final GenerarFacturaPdfUseCase generarFacturaPdfUseCase;
  private final ListarFacturasUseCase listarFacturasUseCase;
  private final ObtenerFacturaXmlUseCase obtenerFacturaXmlUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;
  private final FacturaCommandMapper mapper = new FacturaCommandMapper();

  public FacturaController(
      CrearFacturaUseCase crearFacturaUseCase,
      ConsultarEstadoFacturaUseCase consultarEstadoFacturaUseCase,
      ConsultarFacturaEnProcesoUseCase consultarFacturaEnProcesoUseCase,
      ListarFacturasEnProcesoUseCase listarFacturasEnProcesoUseCase,
      ReenviarFacturasEnProcesoUseCase reenviarFacturasEnProcesoUseCase,
      ReenviarFacturaEnProcesoUseCase reenviarFacturaEnProcesoUseCase,
      GenerarFacturaPdfUseCase generarFacturaPdfUseCase,
      ListarFacturasUseCase listarFacturasUseCase,
      ObtenerFacturaXmlUseCase obtenerFacturaXmlUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.crearFacturaUseCase = crearFacturaUseCase;
    this.consultarEstadoFacturaUseCase = consultarEstadoFacturaUseCase;
    this.consultarFacturaEnProcesoUseCase = consultarFacturaEnProcesoUseCase;
    this.listarFacturasEnProcesoUseCase = listarFacturasEnProcesoUseCase;
    this.reenviarFacturasEnProcesoUseCase = reenviarFacturasEnProcesoUseCase;
    this.reenviarFacturaEnProcesoUseCase = reenviarFacturaEnProcesoUseCase;
    this.generarFacturaPdfUseCase = generarFacturaPdfUseCase;
    this.listarFacturasUseCase = listarFacturasUseCase;
    this.obtenerFacturaXmlUseCase = obtenerFacturaXmlUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @PostMapping
  @Operation(summary = "Crear factura", description = "Crea una factura y la emite al SRI.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Factura creada"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Recursos no encontrados"),
      @ApiResponse(responseCode = "502", description = "Error SRI")
  })
  public ResponseEntity<FacturaCreateResult> crear(@Valid @RequestBody FacturaCreateRequest request) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    if (!empresaId.equals(request.empresaId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    FacturaCreateResult result = crearFacturaUseCase.crear(mapper.toCommand(request, empresaId));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping("/{numeroFactura}/estado")
  @Operation(summary = "Consultar estado", description = "Consulta el estado de una factura en SRI.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Estado consultado"),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Factura no encontrada")
  })
  public ResponseEntity<FacturaEstadoResult> consultarEstado(
      @Parameter(description = "Numero de factura (estab-ptoEmi-secuencial)") @PathVariable String numeroFactura) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    FacturaEstadoResult result = consultarEstadoFacturaUseCase.consultar(
        new ConsultarEstadoFacturaCommand(empresaId, numeroFactura)
    );
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{facturaId}/en-proceso")
  @Operation(summary = "Consultar factura en proceso", description = "Consulta una factura marcada EN_PROCESO.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Factura en proceso"),
      @ApiResponse(responseCode = "400", description = "Factura no esta en proceso"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Factura no encontrada")
  })
  public ResponseEntity<FacturaProcesoResult> consultarEnProceso(
      @Parameter(description = "ID de la factura") @PathVariable Long facturaId) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    return ResponseEntity.ok(consultarFacturaEnProcesoUseCase.consultarEnProceso(facturaId));
  }

  @GetMapping("/empresa/{empresaId}/en-proceso")
  @Operation(summary = "Listar facturas en proceso", description = "Lista facturas EN_PROCESO de una empresa.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de facturas en proceso"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<FacturaProcesoResult>> listarEnProceso(
      @Parameter(description = "ID de la empresa") @PathVariable Long empresaId) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    Long empresaActual = currentUserService.getEmpresaId();
    if (!empresaActual.equals(empresaId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(listarFacturasEnProcesoUseCase.listarEnProceso(empresaId));
  }

  @GetMapping("/empresa/{empresaId}")
  @Operation(summary = "Listar facturas", description = "Lista facturas por empresa y rango de fechas.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de facturas"),
      @ApiResponse(responseCode = "400", description = "Parametros invalidos"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<FacturaResumenPageResult> listarPorEmpresa(
      @Parameter(description = "ID de empresa") @PathVariable Long empresaId,
      @Parameter(description = "Fecha desde (YYYY-MM-DD)") @RequestParam("fechaDesde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
      @Parameter(description = "Fecha hasta (YYYY-MM-DD)") @RequestParam(value = "fechaHasta", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
      @Parameter(description = "Pagina (0-based)") @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @Parameter(description = "Tamano de pagina") @RequestParam(value = "size", required = false, defaultValue = "20") int size
  ) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    Long empresaActual = currentUserService.getEmpresaId();
    if (!empresaActual.equals(empresaId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    int pageSize = Math.min(Math.max(size, 1), 100);
    int pageIndex = Math.max(page, 0);
    return ResponseEntity.ok(listarFacturasUseCase.listarPorEmpresa(empresaId, fechaDesde, fechaHasta, pageIndex, pageSize));
  }

  @PostMapping("/empresa/{empresaId}/en-proceso/reenviar")
  @Operation(summary = "Reenviar en proceso", description = "Reconsulta facturas EN_PROCESO de la empresa.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Facturas reprocesadas"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<FacturaProcesoResult>> reenviarEnProceso(
      @Parameter(description = "ID de la empresa") @PathVariable Long empresaId) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    Long empresaActual = currentUserService.getEmpresaId();
    if (!empresaActual.equals(empresaId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(reenviarFacturasEnProcesoUseCase.reenviarEnProceso(empresaId));
  }

  @PostMapping("/{facturaId}/reenviar")
  @Operation(summary = "Reenviar factura", description = "Reconsulta una factura EN_PROCESO por ID.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Factura reprocesada"),
      @ApiResponse(responseCode = "400", description = "Factura no esta en proceso"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Factura no encontrada")
  })
  public ResponseEntity<FacturaProcesoResult> reenviarFactura(
      @Parameter(description = "ID de la factura") @PathVariable Long facturaId) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    return ResponseEntity.ok(reenviarFacturaEnProcesoUseCase.reenviarEnProceso(facturaId, empresaId));
  }

  @GetMapping("/{facturaId}/pdf")
  @Operation(summary = "Descargar PDF", description = "Genera y devuelve el PDF de la factura.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "PDF generado"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Factura no encontrada")
  })
  public ResponseEntity<byte[]> generarPdf(
      @Parameter(description = "ID de la factura") @PathVariable Long facturaId) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    byte[] pdf = generarFacturaPdfUseCase.generar(new GenerarFacturaPdfCommand(facturaId, empresaId));
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header("Content-Disposition", ContentDisposition.inline().filename("factura-" + facturaId + ".pdf").build().toString())
        .body(pdf);
  }

  @GetMapping("/{facturaId}/xml")
  @Operation(summary = "Descargar XML", description = "Devuelve el XML autorizado de la factura.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "XML generado"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Factura no encontrada")
  })
  public ResponseEntity<byte[]> descargarXml(
      @Parameter(description = "ID de la factura") @PathVariable Long facturaId) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    String xml = obtenerFacturaXmlUseCase.obtenerXml(facturaId, empresaId);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_XML)
        .header("Content-Disposition", ContentDisposition.attachment().filename("factura-" + facturaId + ".xml").build().toString())
        .body(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
}
