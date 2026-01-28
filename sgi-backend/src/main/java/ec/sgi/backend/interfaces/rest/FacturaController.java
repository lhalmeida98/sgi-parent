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
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
import ec.sgi.backend.security.Permisos;
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
public class FacturaController {
  private final CrearFacturaUseCase crearFacturaUseCase;
  private final ConsultarEstadoFacturaUseCase consultarEstadoFacturaUseCase;
  private final ConsultarFacturaEnProcesoUseCase consultarFacturaEnProcesoUseCase;
  private final ListarFacturasEnProcesoUseCase listarFacturasEnProcesoUseCase;
  private final ReenviarFacturasEnProcesoUseCase reenviarFacturasEnProcesoUseCase;
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
    this.generarFacturaPdfUseCase = generarFacturaPdfUseCase;
    this.listarFacturasUseCase = listarFacturasUseCase;
    this.obtenerFacturaXmlUseCase = obtenerFacturaXmlUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @PostMapping
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
  public ResponseEntity<FacturaEstadoResult> consultarEstado(@PathVariable String numeroFactura) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    FacturaEstadoResult result = consultarEstadoFacturaUseCase.consultar(
        new ConsultarEstadoFacturaCommand(empresaId, numeroFactura)
    );
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{facturaId}/en-proceso")
  public ResponseEntity<FacturaProcesoResult> consultarEnProceso(@PathVariable Long facturaId) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    return ResponseEntity.ok(consultarFacturaEnProcesoUseCase.consultarEnProceso(facturaId));
  }

  @GetMapping("/empresa/{empresaId}/en-proceso")
  public ResponseEntity<List<FacturaProcesoResult>> listarEnProceso(@PathVariable Long empresaId) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    Long empresaActual = currentUserService.getEmpresaId();
    if (!empresaActual.equals(empresaId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(listarFacturasEnProcesoUseCase.listarEnProceso(empresaId));
  }

  @GetMapping("/empresa/{empresaId}")
  public ResponseEntity<FacturaResumenPageResult> listarPorEmpresa(
      @PathVariable Long empresaId,
      @RequestParam("fechaDesde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
      @RequestParam(value = "fechaHasta", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
      @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @RequestParam(value = "size", required = false, defaultValue = "20") int size
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
  public ResponseEntity<List<FacturaProcesoResult>> reenviarEnProceso(@PathVariable Long empresaId) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    Long empresaActual = currentUserService.getEmpresaId();
    if (!empresaActual.equals(empresaId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(reenviarFacturasEnProcesoUseCase.reenviarEnProceso(empresaId));
  }

  @GetMapping("/{facturaId}/pdf")
  public ResponseEntity<byte[]> generarPdf(@PathVariable Long facturaId) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    byte[] pdf = generarFacturaPdfUseCase.generar(new GenerarFacturaPdfCommand(facturaId, empresaId));
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header("Content-Disposition", ContentDisposition.inline().filename("factura-" + facturaId + ".pdf").build().toString())
        .body(pdf);
  }

  @GetMapping("/{facturaId}/xml")
  public ResponseEntity<byte[]> descargarXml(@PathVariable Long facturaId) {
    permisoService.requirePermiso(Permisos.FACTURA_GESTION);
    Long empresaId = currentUserService.getEmpresaId();
    String xml = obtenerFacturaXmlUseCase.obtenerXml(facturaId, empresaId);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_XML)
        .header("Content-Disposition", ContentDisposition.attachment().filename("factura-" + facturaId + ".xml").build().toString())
        .body(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
}
