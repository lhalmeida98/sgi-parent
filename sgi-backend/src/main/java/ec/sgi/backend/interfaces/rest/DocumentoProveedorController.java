package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.DocumentoProveedorAutorizacionRequest;
import ec.sgi.backend.application.dto.DocumentoProveedorCreateRequest;
import ec.sgi.backend.application.dto.DocumentoProveedorItemRequest;
import ec.sgi.backend.application.dto.DocumentoProveedorPreviewResult;
import ec.sgi.backend.application.dto.DocumentoProveedorResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.port.in.CrearDocumentoProveedorAutorizacionCommand;
import ec.sgi.backend.application.port.in.CrearDocumentoProveedorAutorizacionUseCase;
import ec.sgi.backend.application.port.in.CrearDocumentoProveedorCommand;
import ec.sgi.backend.application.port.in.CrearDocumentoProveedorUseCase;
import ec.sgi.backend.application.port.in.CrearDocumentoProveedorXmlCommand;
import ec.sgi.backend.application.port.in.CrearDocumentoProveedorXmlUseCase;
import ec.sgi.backend.application.port.in.DocumentoProveedorItemCommand;
import ec.sgi.backend.application.port.in.ListarDocumentosProveedorUseCase;
import ec.sgi.backend.security.CurrentUserService;
import ec.sgi.backend.security.PermisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@Tag(name = "Documentos Proveedor", description = "Registro de documentos de proveedores.")
public class DocumentoProveedorController {
  private final CrearDocumentoProveedorUseCase crearDocumentoProveedorUseCase;
  private final CrearDocumentoProveedorXmlUseCase crearDocumentoProveedorXmlUseCase;
  private final CrearDocumentoProveedorAutorizacionUseCase crearDocumentoProveedorAutorizacionUseCase;
  private final ListarDocumentosProveedorUseCase listarDocumentosProveedorUseCase;
  private final CurrentUserService currentUserService;
  private final PermisoService permisoService;

  public DocumentoProveedorController(
      CrearDocumentoProveedorUseCase crearDocumentoProveedorUseCase,
      CrearDocumentoProveedorXmlUseCase crearDocumentoProveedorXmlUseCase,
      CrearDocumentoProveedorAutorizacionUseCase crearDocumentoProveedorAutorizacionUseCase,
      ListarDocumentosProveedorUseCase listarDocumentosProveedorUseCase,
      CurrentUserService currentUserService,
      PermisoService permisoService
  ) {
    this.crearDocumentoProveedorUseCase = crearDocumentoProveedorUseCase;
    this.crearDocumentoProveedorXmlUseCase = crearDocumentoProveedorXmlUseCase;
    this.crearDocumentoProveedorAutorizacionUseCase = crearDocumentoProveedorAutorizacionUseCase;
    this.listarDocumentosProveedorUseCase = listarDocumentosProveedorUseCase;
    this.currentUserService = currentUserService;
    this.permisoService = permisoService;
  }

  @GetMapping("/documentos-proveedor")
  @Operation(summary = "Listar documentos", description = "Lista documentos de proveedores."
      + " Puede filtrar por proveedorId.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de documentos"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos")
  })
  public ResponseEntity<List<DocumentoProveedorResult>> listar(
      @Parameter(description = "ID del proveedor") @RequestParam(required = false) Long proveedorId
  ) {
    permisoService.requirePermiso("PROVEEDORES");
    Long empresaId = currentUserService.getEmpresaId();
    return ResponseEntity.ok(listarDocumentosProveedorUseCase.listar(empresaId, proveedorId));
  }

  @GetMapping("/proveedores/{proveedorId}/documentos")
  @Operation(summary = "Listar documentos por proveedor", description = "Lista documentos de un proveedor.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Listado de documentos"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
  })
  public ResponseEntity<List<DocumentoProveedorResult>> listarPorProveedor(
      @Parameter(description = "ID del proveedor") @PathVariable Long proveedorId
  ) {
    permisoService.requirePermiso("PROVEEDORES");
    Long empresaId = currentUserService.getEmpresaId();
    return ResponseEntity.ok(listarDocumentosProveedorUseCase.listar(empresaId, proveedorId));
  }

  @PostMapping("/proveedores/{proveedorId}/documentos")
  @Operation(summary = "Registrar documento", description = "Registra un documento de proveedor (manual). "
      + "Si un item no tiene productoId y no existe por codigo, se crea un producto automatico.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Documento registrado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
  })
  public ResponseEntity<DocumentoProveedorResult> crear(
      @Parameter(description = "ID del proveedor") @PathVariable Long proveedorId,
      @Valid @RequestBody DocumentoProveedorCreateRequest request
  ) {
    permisoService.requirePermiso("PROVEEDORES");
    Long empresaId = currentUserService.getEmpresaId();
    DocumentoProveedorResult result = crearDocumentoProveedorUseCase.crear(new CrearDocumentoProveedorCommand(
        empresaId,
        proveedorId,
        request.tipoDocumento(),
        request.numeroDocumento(),
        request.numeroAutorizacion(),
        request.fechaEmision(),
        request.fechaVencimiento(),
        request.subtotal(),
        request.impuestos(),
        request.total(),
        request.moneda(),
        mapItems(request.items())
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @PostMapping("/proveedores/{proveedorId}/documentos/confirmar")
  @Operation(summary = "Confirmar documento", description = "Guarda el documento de proveedor usando el JSON final (con productos y bodegas). "
      + "Si un item no tiene productoId y no existe por codigo, se crea un producto automatico.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Documento registrado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Proveedor o bodega no encontrada")
  })
  public ResponseEntity<DocumentoProveedorResult> confirmar(
      @Parameter(description = "ID del proveedor") @PathVariable Long proveedorId,
      @Valid @RequestBody DocumentoProveedorCreateRequest request
  ) {
    permisoService.requirePermiso("PROVEEDORES");
    Long empresaId = currentUserService.getEmpresaId();
    DocumentoProveedorResult result = crearDocumentoProveedorUseCase.crear(new CrearDocumentoProveedorCommand(
        empresaId,
        proveedorId,
        request.tipoDocumento(),
        request.numeroDocumento(),
        request.numeroAutorizacion(),
        request.fechaEmision(),
        request.fechaVencimiento(),
        request.subtotal(),
        request.impuestos(),
        request.total(),
        request.moneda(),
        mapItems(request.items())
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @PostMapping(path = "/proveedores/{proveedorId}/documentos/xml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Registrar documento desde XML", description = "Registra un documento de proveedor desde XML.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Documento registrado"),
      @ApiResponse(responseCode = "400", description = "Archivo invalido"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
  })
  public ResponseEntity<DocumentoProveedorPreviewResult> crearDesdeXml(
      @Parameter(description = "ID del proveedor") @PathVariable Long proveedorId,
      @Parameter(description = "Archivo XML") @RequestParam("archivo") MultipartFile archivo,
      @Parameter(description = "ID de la bodega") @RequestParam(value = "bodegaId", required = false) Long bodegaId
  ) {
    permisoService.requirePermiso("PROVEEDORES");
    String xml;
    try {
      xml = new String(archivo.getBytes(), StandardCharsets.UTF_8);
    } catch (Exception ex) {
      throw new BusinessRuleException("No se pudo leer el XML");
    }
    DocumentoProveedorPreviewResult result = crearDocumentoProveedorXmlUseCase.crearDesdeXml(
        new CrearDocumentoProveedorXmlCommand(
            currentUserService.getEmpresaId(),
            proveedorId,
            bodegaId,
            xml
        )
    );
    return ResponseEntity.ok(result);
  }

  @PostMapping("/proveedores/{proveedorId}/documentos/autorizacion")
  @Operation(summary = "Registrar documento desde autorizacion", description = "Registra un documento de proveedor usando numero de autorizacion SRI.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Documento registrado"),
      @ApiResponse(responseCode = "400", description = "Validacion invalida"),
      @ApiResponse(responseCode = "401", description = "No autorizado"),
      @ApiResponse(responseCode = "403", description = "Sin permisos"),
      @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
  })
  public ResponseEntity<DocumentoProveedorPreviewResult> crearDesdeAutorizacion(
      @Parameter(description = "ID del proveedor") @PathVariable Long proveedorId,
      @Parameter(description = "ID de la bodega") @RequestParam(value = "bodegaId", required = false) Long bodegaId,
      @Valid @RequestBody DocumentoProveedorAutorizacionRequest request
  ) {
    permisoService.requirePermiso("PROVEEDORES");
    DocumentoProveedorPreviewResult result = crearDocumentoProveedorAutorizacionUseCase.crearDesdeAutorizacion(
        new CrearDocumentoProveedorAutorizacionCommand(
            currentUserService.getEmpresaId(),
            proveedorId,
            bodegaId,
            request.numeroAutorizacion()
        )
    );
    return ResponseEntity.ok(result);
  }

  private List<DocumentoProveedorItemCommand> mapItems(List<DocumentoProveedorItemRequest> items) {
    if (items == null || items.isEmpty()) {
      return Collections.emptyList();
    }
    return items.stream()
        .map(item -> new DocumentoProveedorItemCommand(
            item.bodegaId(),
            item.productoId(),
            item.categoriaId(),
            item.impuestoId(),
            item.codigoPrincipal(),
            item.descripcion(),
            item.precioVenta(),
            item.cantidad(),
            item.costoUnitario(),
            item.subtotal()
        ))
        .toList();
  }
}
