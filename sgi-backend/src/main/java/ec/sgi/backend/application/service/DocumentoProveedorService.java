package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.DocumentoProveedorResult;
import ec.sgi.backend.application.dto.DocumentoProveedorPreviewItemResult;
import ec.sgi.backend.application.dto.DocumentoProveedorPreviewResult;
import ec.sgi.backend.application.dto.SriConsultaEstadoRequest;
import ec.sgi.backend.application.dto.SriConsultaEstadoResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.CrearDocumentoProveedorAutorizacionCommand;
import ec.sgi.backend.application.port.in.CrearDocumentoProveedorAutorizacionUseCase;
import ec.sgi.backend.application.port.in.CrearDocumentoProveedorCommand;
import ec.sgi.backend.application.port.in.CrearDocumentoProveedorUseCase;
import ec.sgi.backend.application.port.in.CrearDocumentoProveedorXmlCommand;
import ec.sgi.backend.application.port.in.CrearDocumentoProveedorXmlUseCase;
import ec.sgi.backend.application.port.in.DocumentoProveedorItemCommand;
import ec.sgi.backend.application.port.in.ListarDocumentosProveedorUseCase;
import ec.sgi.backend.application.port.out.CuentaPorPagarRepository;
import ec.sgi.backend.application.port.out.DocumentoProveedorRepository;
import ec.sgi.backend.application.port.out.CategoriaRepository;
import ec.sgi.backend.application.port.out.BodegaRepository;
import ec.sgi.backend.application.port.out.EmpresaRepository;
import ec.sgi.backend.application.port.out.ImpuestoRepository;
import ec.sgi.backend.application.port.out.InventarioRepository;
import ec.sgi.backend.application.port.out.ProductoRepository;
import ec.sgi.backend.application.port.out.ProveedorRepository;
import ec.sgi.backend.application.port.out.SriCorePort;
import ec.sgi.backend.domain.model.Categoria;
import ec.sgi.backend.domain.model.CuentaPorPagar;
import ec.sgi.backend.domain.model.DocumentoProveedor;
import ec.sgi.backend.domain.model.DocumentoProveedorItem;
import ec.sgi.backend.domain.model.Empresa;
import ec.sgi.backend.domain.model.Impuesto;
import ec.sgi.backend.domain.model.Inventario;
import ec.sgi.backend.domain.model.Producto;
import ec.sgi.backend.domain.model.Proveedor;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
@Transactional
public class DocumentoProveedorService implements CrearDocumentoProveedorUseCase,
    CrearDocumentoProveedorXmlUseCase, CrearDocumentoProveedorAutorizacionUseCase,
    ListarDocumentosProveedorUseCase {
  private static final String ESTADO_REGISTRADO = "REGISTRADO";
  private static final String ESTADO_PARCIAL = "PARCIAL";
  private static final String ESTADO_PAGADO = "PAGADO";
  private static final String ESTADO_PENDIENTE = "PENDIENTE";
  private static final String TIPO_FACTURA = "FACTURA";
  private static final String TIPO_NC = "NC";
  private static final String TIPO_ND = "ND";
  private static final Pattern DIAS_PATTERN = Pattern.compile("(\\d+)");

  private final DocumentoProveedorRepository documentoProveedorRepository;
  private final ProveedorRepository proveedorRepository;
  private final CuentaPorPagarRepository cuentaPorPagarRepository;
  private final ProductoRepository productoRepository;
  private final CategoriaRepository categoriaRepository;
  private final ImpuestoRepository impuestoRepository;
  private final InventarioRepository inventarioRepository;
  private final SriCorePort sriCorePort;
  private final EmpresaRepository empresaRepository;
  private final BodegaRepository bodegaRepository;

  public DocumentoProveedorService(
      DocumentoProveedorRepository documentoProveedorRepository,
      ProveedorRepository proveedorRepository,
      CuentaPorPagarRepository cuentaPorPagarRepository,
      ProductoRepository productoRepository,
      CategoriaRepository categoriaRepository,
      ImpuestoRepository impuestoRepository,
      InventarioRepository inventarioRepository,
      SriCorePort sriCorePort,
      EmpresaRepository empresaRepository,
      BodegaRepository bodegaRepository
  ) {
    this.documentoProveedorRepository = documentoProveedorRepository;
    this.proveedorRepository = proveedorRepository;
    this.cuentaPorPagarRepository = cuentaPorPagarRepository;
    this.productoRepository = productoRepository;
    this.categoriaRepository = categoriaRepository;
    this.impuestoRepository = impuestoRepository;
    this.inventarioRepository = inventarioRepository;
    this.sriCorePort = sriCorePort;
    this.empresaRepository = empresaRepository;
    this.bodegaRepository = bodegaRepository;
  }

  @Override
  public DocumentoProveedorResult crear(CrearDocumentoProveedorCommand command) {
    Proveedor proveedor = obtenerProveedor(command.empresaId(), command.proveedorId());
    String tipo = normalizarTipo(command.tipoDocumento());
    validarDocumentoUnico(command.empresaId(), proveedor.id(), command.numeroDocumento(), command.numeroAutorizacion());
    LocalDate fechaVencimiento = calcularFechaVencimiento(command.fechaEmision(), command.fechaVencimiento(), proveedor);
    List<DocumentoProveedorItem> items = mapItems(command.empresaId(), proveedor.id(), command.items());
    if (TIPO_FACTURA.equals(tipo) && items.isEmpty()) {
      throw new BusinessRuleException("Factura de proveedor requiere items para actualizar inventario");
    }
    DocumentoProveedor documento = new DocumentoProveedor(
        null,
        command.empresaId(),
        proveedor.id(),
        tipo,
        command.numeroDocumento(),
        command.numeroAutorizacion(),
        command.fechaEmision(),
        fechaVencimiento,
        command.subtotal(),
        command.impuestos(),
        command.total(),
        command.moneda(),
        ESTADO_REGISTRADO,
        null,
        items,
        null,
        null
    );
    DocumentoProveedor guardado = documentoProveedorRepository.save(documento);
    procesarCuentaPorPagarYInventario(guardado, proveedor);
    return toResult(guardado);
  }

  @Override
  public DocumentoProveedorPreviewResult crearDesdeXml(CrearDocumentoProveedorXmlCommand command) {
    Proveedor proveedor = obtenerProveedor(command.empresaId(), command.proveedorId());
    String xml = requireXml(command.xml());
    ParsedDocumento parsed = parseDocumento(xml, command.bodegaId());
    validarProveedorXml(proveedor, parsed.rucProveedor, parsed.razonSocialProveedor);
    return toPreviewResult(parsed);
  }

  @Override
  public DocumentoProveedorPreviewResult crearDesdeAutorizacion(CrearDocumentoProveedorAutorizacionCommand command) {
    Proveedor proveedor = obtenerProveedor(command.empresaId(), command.proveedorId());
    Empresa empresa = empresaRepository.findById(command.empresaId())
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
    SriConsultaEstadoResult sriResult = sriCorePort.consultarEstado(new SriConsultaEstadoRequest(
        empresa.ambiente(),
        command.numeroAutorizacion()
    ));
    if (!isAutorizado(sriResult)) {
      String mensaje = sriResult.mensaje() == null ? "Comprobante no autorizado" : sriResult.mensaje();
      throw new BusinessRuleException(mensaje);
    }
    String xml = requireXml(sriResult.xmlAutorizado());
    ParsedDocumento parsed = parseDocumento(xml, command.bodegaId());
    validarProveedorXml(proveedor, parsed.rucProveedor, parsed.razonSocialProveedor);
    return toPreviewResult(parsed);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DocumentoProveedorResult> listar(Long empresaId, Long proveedorId) {
    List<DocumentoProveedor> documentos;
    if (proveedorId == null) {
      documentos = documentoProveedorRepository.findByEmpresaId(empresaId);
    } else {
      obtenerProveedor(empresaId, proveedorId);
      documentos = documentoProveedorRepository.findByProveedorId(proveedorId);
    }
    return documentos.stream().map(this::toResult).toList();
  }

  private void procesarCuentaPorPagarYInventario(DocumentoProveedor documento, Proveedor proveedor) {
    if (documento == null) {
      return;
    }
    String tipo = normalizarTipo(documento.tipoDocumento());
    if (TIPO_FACTURA.equals(tipo) || TIPO_ND.equals(tipo)) {
      CuentaPorPagar cuenta = new CuentaPorPagar(
          null,
          documento.empresaId(),
          documento.proveedorId(),
          documento.id(),
          documento.total(),
          BigDecimal.ZERO,
          documento.total(),
          ESTADO_PENDIENTE,
          documento.fechaVencimiento(),
          null,
          null
      );
      cuentaPorPagarRepository.save(cuenta);
      if (TIPO_FACTURA.equals(tipo)) {
        ajustarInventario(documento.empresaId(), documento.items());
      }
      return;
    }
    if (TIPO_NC.equals(tipo)) {
      aplicarNotaCredito(documento, proveedor);
    }
  }

  private void aplicarNotaCredito(DocumentoProveedor documento, Proveedor proveedor) {
    BigDecimal monto = documento.total();
    if (monto.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessRuleException("Nota de credito debe tener total positivo");
    }
    List<CuentaPorPagar> cuentas = cuentaPorPagarRepository.findByProveedorId(proveedor.id()).stream()
        .filter(cuenta -> cuenta.saldo().compareTo(BigDecimal.ZERO) > 0)
        .sorted(Comparator.comparing(CuentaPorPagar::fechaVencimiento, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(CuentaPorPagar::id))
        .toList();
    BigDecimal saldoTotal = cuentas.stream()
        .map(CuentaPorPagar::saldo)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (saldoTotal.compareTo(monto) < 0) {
      throw new BusinessRuleException("Nota de credito excede el saldo pendiente del proveedor");
    }
    BigDecimal restante = monto;
    for (CuentaPorPagar cuenta : cuentas) {
      if (restante.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
      BigDecimal aplicar = cuenta.saldo().min(restante);
      BigDecimal nuevoPagado = cuenta.montoPagado().add(aplicar);
      BigDecimal nuevoSaldo = cuenta.montoOriginal().subtract(nuevoPagado);
      String estado = nuevoSaldo.compareTo(BigDecimal.ZERO) == 0 ? ESTADO_PAGADO : ESTADO_PARCIAL;
      CuentaPorPagar actualizada = new CuentaPorPagar(
          cuenta.id(),
          cuenta.empresaId(),
          cuenta.proveedorId(),
          cuenta.documentoProveedorId(),
          cuenta.montoOriginal(),
          nuevoPagado,
          nuevoSaldo,
          estado,
          cuenta.fechaVencimiento(),
          cuenta.creadoEn(),
          LocalDateTime.now()
      );
      cuentaPorPagarRepository.save(actualizada);
      actualizarEstadoDocumento(actualizada.documentoProveedorId(), estado);
      restante = restante.subtract(aplicar);
    }
    actualizarEstadoDocumento(documento.id(), ESTADO_PAGADO);
  }

  private void ajustarInventario(Long empresaId, List<DocumentoProveedorItem> items) {
    if (items == null || items.isEmpty()) {
      return;
    }
    LocalDateTime ahora = LocalDateTime.now();
    for (DocumentoProveedorItem item : items) {
      Long bodegaId = item.bodegaId();
      Long productoId = item.productoId();
      if (productoId == null) {
        productoId = resolverProductoId(empresaId, item.codigoPrincipal());
      }
      if (productoId == null) {
        throw new BusinessRuleException("Producto no encontrado para item " + item.codigoPrincipal());
      }
      Inventario inventario = inventarioRepository.findByProductoIdAndEmpresaIdAndBodegaIdForUpdate(
          productoId,
          empresaId,
          bodegaId
      )
          .orElse(null);
      BigDecimal cantidad = item.cantidad();
      BigDecimal costoUnitario = item.costoUnitario();
      if (inventario == null) {
        Inventario nuevo = new Inventario(
            null,
            empresaId,
            bodegaId,
            productoId,
            cantidad,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            null,
            null,
            costoUnitario,
            ahora
        );
        inventarioRepository.save(nuevo);
        continue;
      }
      BigDecimal stockActual = inventario.stockActual();
      BigDecimal nuevoStock = stockActual.add(cantidad);
      BigDecimal costoPromedioActual = inventario.costoPromedio() == null ? BigDecimal.ZERO : inventario.costoPromedio();
      BigDecimal nuevoCosto = calcularCostoPromedio(costoPromedioActual, stockActual, costoUnitario, cantidad);
      Inventario actualizado = inventario
          .withStockActual(nuevoStock)
          .withCostoPromedio(nuevoCosto)
          .withActualizadoEn(ahora);
      inventarioRepository.save(actualizado);
    }
  }

  private BigDecimal calcularCostoPromedio(BigDecimal costoActual, BigDecimal stockActual,
      BigDecimal costoUnitario, BigDecimal cantidad) {
    BigDecimal denominador = stockActual.add(cantidad);
    if (denominador.compareTo(BigDecimal.ZERO) <= 0) {
      return costoActual;
    }
    BigDecimal totalActual = costoActual.multiply(stockActual);
    BigDecimal totalNuevo = costoUnitario.multiply(cantidad);
    return totalActual.add(totalNuevo).divide(denominador, 4, RoundingMode.HALF_UP);
  }

  private void actualizarEstadoDocumento(Long documentoProveedorId, String estadoCxP) {
    if (documentoProveedorId == null) {
      return;
    }
    DocumentoProveedor documento = documentoProveedorRepository.findById(documentoProveedorId).orElse(null);
    if (documento == null) {
      return;
    }
    String nuevoEstado;
    if (ESTADO_PAGADO.equals(estadoCxP)) {
      nuevoEstado = ESTADO_PAGADO;
    } else if (ESTADO_PARCIAL.equals(estadoCxP)) {
      nuevoEstado = ESTADO_PARCIAL;
    } else {
      nuevoEstado = ESTADO_REGISTRADO;
    }
    DocumentoProveedor actualizado = new DocumentoProveedor(
        documento.id(),
        documento.empresaId(),
        documento.proveedorId(),
        documento.tipoDocumento(),
        documento.numeroDocumento(),
        documento.numeroAutorizacion(),
        documento.fechaEmision(),
        documento.fechaVencimiento(),
        documento.subtotal(),
        documento.impuestos(),
        documento.total(),
        documento.moneda(),
        nuevoEstado,
        documento.xml(),
        documento.items(),
        documento.creadoEn(),
        LocalDateTime.now()
    );
    documentoProveedorRepository.save(actualizado);
  }

  private void validarDocumentoUnico(Long empresaId, Long proveedorId, String numeroDocumento, String numeroAutorizacion) {
    if (numeroDocumento != null
        && documentoProveedorRepository.existsByEmpresaIdAndProveedorIdAndNumeroDocumento(
        empresaId, proveedorId, numeroDocumento)) {
      throw new BusinessRuleException("Documento ya registrado con ese numero");
    }
    if (numeroAutorizacion != null && !numeroAutorizacion.isBlank()) {
      if (documentoProveedorRepository.findByEmpresaIdAndNumeroAutorizacion(empresaId, numeroAutorizacion).isPresent()) {
        throw new BusinessRuleException("Documento ya registrado con ese numero de autorizacion");
      }
    }
  }

  private Proveedor obtenerProveedor(Long empresaId, Long proveedorId) {
    return proveedorRepository.findByIdAndEmpresaId(proveedorId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
  }

  private void validarProveedorXml(Proveedor proveedor, String rucXml, String razonSocialXml) {
    if (rucXml == null || rucXml.isBlank()) {
      return;
    }
    if (!proveedor.identificacion().trim().equals(rucXml.trim())) {
      throw new BusinessRuleException("El RUC del XML no coincide con el proveedor seleccionado");
    }
  }

  private String normalizarTipo(String tipo) {
    if (tipo == null) {
      throw new BusinessRuleException("Tipo de documento requerido");
    }
    String normalizado = tipo.trim().toUpperCase(Locale.ROOT);
    return switch (normalizado) {
      case "FACTURA", "01" -> TIPO_FACTURA;
      case "NC", "NOTA_CREDITO", "04" -> TIPO_NC;
      case "ND", "NOTA_DEBITO", "05" -> TIPO_ND;
      default -> throw new BusinessRuleException("Tipo de documento no soportado: " + tipo);
    };
  }

  private DocumentoProveedorResult toResult(DocumentoProveedor documento) {
    return new DocumentoProveedorResult(
        documento.id(),
        documento.proveedorId(),
        documento.tipoDocumento(),
        documento.numeroDocumento(),
        documento.numeroAutorizacion(),
        documento.fechaEmision(),
        documento.fechaVencimiento(),
        documento.total(),
        documento.moneda(),
        documento.estado()
    );
  }

  private DocumentoProveedorPreviewResult toPreviewResult(ParsedDocumento parsed) {
    List<DocumentoProveedorPreviewItemResult> items = parsed.items.stream()
        .map(item -> new DocumentoProveedorPreviewItemResult(
            item.bodegaId(),
            item.codigoPrincipal(),
            item.descripcion(),
            item.cantidad(),
            item.costoUnitario(),
            item.subtotal()
        ))
        .toList();
    return new DocumentoProveedorPreviewResult(
        parsed.tipoDocumento,
        parsed.numeroDocumento,
        parsed.numeroAutorizacion,
        parsed.fechaEmision,
        parsed.subtotal,
        parsed.impuestos,
        parsed.total,
        parsed.moneda,
        parsed.rucProveedor,
        parsed.razonSocialProveedor,
        items
    );
  }

  private LocalDate calcularFechaVencimiento(LocalDate fechaEmision, LocalDate fechaVencimiento, Proveedor proveedor) {
    if (fechaVencimiento != null) {
      return fechaVencimiento;
    }
    if (fechaEmision == null || proveedor == null) {
      return fechaVencimiento;
    }
    String condiciones = proveedor.condicionesPago();
    if (condiciones == null || condiciones.isBlank()) {
      return fechaEmision;
    }
    String upper = condiciones.toUpperCase(Locale.ROOT);
    if (upper.contains("CONTADO")) {
      return fechaEmision;
    }
    Matcher matcher = DIAS_PATTERN.matcher(upper);
    if (matcher.find()) {
      int dias = Integer.parseInt(matcher.group(1));
      return fechaEmision.plusDays(dias);
    }
    return fechaEmision;
  }

  private List<DocumentoProveedorItem> mapItems(Long empresaId, Long proveedorId, List<DocumentoProveedorItemCommand> items) {
    if (items == null || items.isEmpty()) {
      return List.of();
    }
    List<DocumentoProveedorItem> mapped = new ArrayList<>();
    for (DocumentoProveedorItemCommand item : items) {
      Long bodegaId = item.bodegaId();
      if (bodegaId == null) {
        throw new BusinessRuleException("Bodega requerida para registrar items");
      }
      ensureBodegaExists(empresaId, bodegaId);
      Long productoId = resolveOrCreateProductoId(empresaId, proveedorId, item);
      mapped.add(new DocumentoProveedorItem(
          null,
          bodegaId,
          productoId,
          item.codigoPrincipal(),
          item.descripcion(),
          item.cantidad(),
          item.costoUnitario(),
          item.subtotal()
      ));
    }
    return mapped;
  }

  private Long resolverProductoId(Long empresaId, String codigoPrincipal) {
    if (codigoPrincipal == null || codigoPrincipal.isBlank()) {
      return null;
    }
    Optional<Producto> producto = productoRepository.findByEmpresaIdAndCodigo(empresaId, codigoPrincipal);
    if (producto.isPresent()) {
      return producto.get().id();
    }
    return productoRepository.findByEmpresaIdAndCodigoBarras(empresaId, codigoPrincipal)
        .map(Producto::id)
        .orElse(null);
  }

  private Long resolveOrCreateProductoId(Long empresaId, Long proveedorId, DocumentoProveedorItemCommand item) {
    if (item.productoId() != null) {
      Producto producto = productoRepository.findByIdAndEmpresaId(item.productoId(), empresaId).orElse(null);
      if (producto == null) {
        throw new BusinessRuleException("Producto no encontrado para item " + safeCodigo(item.codigoPrincipal()));
      }
      actualizarPrecioVentaSiAplica(producto, item.precioVenta());
      return producto.id();
    }
    Long existente = resolverProductoId(empresaId, item.codigoPrincipal());
    if (existente != null) {
      Producto producto = productoRepository.findByIdAndEmpresaId(existente, empresaId).orElse(null);
      actualizarPrecioVentaSiAplica(producto, item.precioVenta());
      return existente;
    }
    Long categoriaId = resolveCategoriaId(empresaId, item.categoriaId());
    Long impuestoId = resolveImpuestoId(empresaId, item.impuestoId());
    BigDecimal precioVenta = resolvePrecioVenta(item.precioVenta());
    String codigo = normalizeCodigo(item.codigoPrincipal());
    String descripcion = normalizeDescripcion(item.descripcion(), codigo);
    Producto nuevo = new Producto(
        null,
        empresaId,
        codigo,
        descripcion,
        precioVenta,
        categoriaId,
        impuestoId,
        proveedorId,
        true,
        null
    );
    return productoRepository.save(nuevo).id();
  }

  private Long resolveCategoriaId(Long empresaId, Long categoriaId) {
    if (categoriaId == null) {
      throw new BusinessRuleException("Categoria requerida para crear producto nuevo");
    }
    return categoriaRepository.findByIdAndEmpresaId(categoriaId, empresaId)
        .map(Categoria::id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));
  }

  private Long resolveImpuestoId(Long empresaId, Long impuestoId) {
    if (impuestoId == null) {
      throw new BusinessRuleException("Impuesto requerido para crear producto nuevo");
    }
    return impuestoRepository.findByIdAndEmpresaId(impuestoId, empresaId)
        .map(Impuesto::id)
        .orElseThrow(() -> new ResourceNotFoundException("Impuesto no encontrado"));
  }

  private BigDecimal resolvePrecioVenta(BigDecimal precioVenta) {
    if (precioVenta == null) {
      throw new BusinessRuleException("Precio de venta requerido para crear producto nuevo");
    }
    if (precioVenta.compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessRuleException("Precio de venta invalido");
    }
    return precioVenta;
  }

  private void actualizarPrecioVentaSiAplica(Producto producto, BigDecimal precioVenta) {
    if (producto == null || precioVenta == null) {
      return;
    }
    if (precioVenta.compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessRuleException("Precio de venta invalido");
    }
    if (producto.precioUnitario().compareTo(precioVenta) == 0) {
      return;
    }
    Producto actualizado = new Producto(
        producto.id(),
        producto.empresaId(),
        producto.codigo(),
        producto.descripcion(),
        precioVenta,
        producto.categoriaId(),
        producto.impuestoId(),
        producto.proveedorId(),
        producto.vendible(),
        producto.codigoBarras()
    );
    productoRepository.save(actualizado);
  }

  private String normalizeCodigo(String codigoPrincipal) {
    String codigo = codigoPrincipal == null ? "" : codigoPrincipal.trim();
    if (codigo.isBlank()) {
      codigo = "AUTO-" + UUID.randomUUID().toString().replace("-", "");
    }
    if (codigo.length() > 50) {
      return codigo.substring(0, 50);
    }
    return codigo;
  }

  private String normalizeDescripcion(String descripcion, String codigo) {
    String value = descripcion == null ? "" : descripcion.trim();
    if (value.isBlank()) {
      value = "Producto " + codigo;
    }
    if (value.length() > 255) {
      return value.substring(0, 255);
    }
    return value;
  }

  private String safeCodigo(String codigoPrincipal) {
    if (codigoPrincipal == null || codigoPrincipal.isBlank()) {
      return "SIN-CODIGO";
    }
    return codigoPrincipal.trim();
  }

  private boolean isAutorizado(SriConsultaEstadoResult result) {
    if (result == null) {
      return false;
    }
    String estado = result.estadoAutorizacion();
    return estado != null && estado.equalsIgnoreCase("AUTORIZADO");
  }

  private String requireXml(String xml) {
    if (xml == null || xml.isBlank()) {
      throw new BusinessRuleException("XML no disponible para registrar");
    }
    return xml.strip();
  }

  private ParsedDocumento parseDocumento(String xmlOriginal) {
    return parseDocumento(xmlOriginal, null);
  }

  private ParsedDocumento parseDocumento(String xmlOriginal, Long bodegaId) {
    String xml = extractComprobanteXml(xmlOriginal);
    Document doc = parseXmlDocument(xml);
    Element root = doc.getDocumentElement();
    String rootName = root == null ? null : root.getNodeName();
    String tipo = resolveTipoDocumento(rootName, doc);
    LocalDate fechaEmision = parseFecha(firstText(doc, "fechaEmision"));
    String moneda = firstText(doc, "moneda");
    if (moneda == null || moneda.isBlank()) {
      moneda = "USD";
    }
    BigDecimal subtotal = parseDecimal(firstText(doc, "totalSinImpuestos"));
    BigDecimal total = parseDecimal(firstText(doc, "importeTotal"));
    if (total == null) {
      total = parseDecimal(firstText(doc, "valorModificacion"));
    }
    if (total == null) {
      total = parseDecimal(firstText(doc, "valorTotal"));
    }
    BigDecimal impuestos = sumImpuestos(doc);
    if (impuestos == null && subtotal != null && total != null) {
      impuestos = total.subtract(subtotal);
    }
    String estab = firstText(doc, "estab");
    String ptoEmi = firstText(doc, "ptoEmi");
    String secuencial = firstText(doc, "secuencial");
    String numeroDocumento = buildNumeroDocumento(estab, ptoEmi, secuencial);
    String numeroAutorizacion = firstText(doc, "numeroAutorizacion");
    if (numeroAutorizacion == null || numeroAutorizacion.isBlank()) {
      numeroAutorizacion = firstText(doc, "claveAcceso");
    }
    String ruc = firstText(doc, "ruc");
    String razonSocial = firstText(doc, "razonSocial");
    List<DocumentoProveedorItemCommand> items = parseItems(doc, bodegaId);
    if (fechaEmision == null) {
      throw new BusinessRuleException("No se pudo leer fecha de emision del XML");
    }
    if (numeroDocumento == null || numeroDocumento.isBlank()) {
      throw new BusinessRuleException("No se pudo leer numero de documento del XML");
    }
    if (subtotal == null || total == null) {
      throw new BusinessRuleException("No se pudo leer totales del XML");
    }
    if (impuestos == null) {
      impuestos = BigDecimal.ZERO;
    }
    return new ParsedDocumento(tipo, numeroDocumento, numeroAutorizacion, fechaEmision, subtotal, impuestos, total,
        moneda, ruc, razonSocial, items);
  }

  private String resolveTipoDocumento(String rootName, Document doc) {
    if (rootName != null) {
      String nombre = rootName.contains(":")
          ? rootName.substring(rootName.indexOf(':') + 1)
          : rootName;
      switch (nombre) {
        case "factura":
          return TIPO_FACTURA;
        case "notaCredito":
          return TIPO_NC;
        case "notaDebito":
          return TIPO_ND;
        default:
          break;
      }
    }
    String codDoc = firstText(doc, "codDoc");
    if (codDoc == null) {
      throw new BusinessRuleException("No se pudo determinar tipo de documento");
    }
    return normalizarTipo(codDoc);
  }

  private List<DocumentoProveedorItemCommand> parseItems(Document doc, Long bodegaId) {
    List<DocumentoProveedorItemCommand> items = new ArrayList<>();
    Long resolvedBodegaId = bodegaId;
    NodeList detalles = doc.getElementsByTagName("detalle");
    for (int i = 0; i < detalles.getLength(); i++) {
      Node node = detalles.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      Element detalle = (Element) node;
      String codigo = textOfChild(detalle, "codigoPrincipal");
      String descripcion = textOfChild(detalle, "descripcion");
      BigDecimal cantidad = parseDecimal(textOfChild(detalle, "cantidad"));
      BigDecimal costoUnitario = parseDecimal(textOfChild(detalle, "precioUnitario"));
      BigDecimal subtotal = parseDecimal(textOfChild(detalle, "precioTotalSinImpuesto"));
      if (subtotal == null && cantidad != null && costoUnitario != null) {
        subtotal = cantidad.multiply(costoUnitario);
      }
      if (cantidad == null || costoUnitario == null || subtotal == null) {
        continue;
      }
      items.add(new DocumentoProveedorItemCommand(
          resolvedBodegaId,
          null,
          null,
          null,
          codigo,
          descripcion,
          null,
          cantidad,
          costoUnitario,
          subtotal
      ));
    }
    return items;
  }

  private BigDecimal sumImpuestos(Document doc) {
    NodeList totalImpuestos = doc.getElementsByTagName("totalImpuesto");
    if (totalImpuestos == null || totalImpuestos.getLength() == 0) {
      return null;
    }
    BigDecimal total = BigDecimal.ZERO;
    for (int i = 0; i < totalImpuestos.getLength(); i++) {
      Node node = totalImpuestos.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      Element impuesto = (Element) node;
      BigDecimal valor = parseDecimal(textOfChild(impuesto, "valor"));
      if (valor != null) {
        total = total.add(valor);
      }
    }
    return total;
  }

  private String extractComprobanteXml(String xml) {
    String candidate = xml == null ? "" : xml.trim();
    if (candidate.isBlank()) {
      return candidate;
    }
    try {
      Document doc = parseXmlDocument(candidate);
      NodeList comprobantes = doc.getElementsByTagName("comprobante");
      if (comprobantes != null && comprobantes.getLength() > 0) {
        String inner = comprobantes.item(0).getTextContent();
        if (inner != null && inner.contains("<")) {
          return inner.trim();
        }
      }
    } catch (RuntimeException ex) {
      return candidate;
    }
    return candidate;
  }

  private Document parseXmlDocument(String xml) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    } catch (Exception ex) {
      throw new BusinessRuleException("XML de proveedor invalido");
    }
  }

  private String firstText(Document doc, String tagName) {
    if (doc == null) {
      return null;
    }
    NodeList nodes = doc.getElementsByTagName(tagName);
    if (nodes == null || nodes.getLength() == 0) {
      return null;
    }
    return nodes.item(0).getTextContent();
  }

  private String textOfChild(Element parent, String tagName) {
    if (parent == null) {
      return null;
    }
    NodeList nodes = parent.getElementsByTagName(tagName);
    if (nodes == null || nodes.getLength() == 0) {
      return null;
    }
    return nodes.item(0).getTextContent();
  }

  private LocalDate parseFecha(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    String value = raw.trim();
    List<DateTimeFormatter> formatos = List.of(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ISO_LOCAL_DATE
    );
    for (DateTimeFormatter formatter : formatos) {
      try {
        return LocalDate.parse(value, formatter);
      } catch (DateTimeParseException ex) {
        // continue
      }
    }
    return null;
  }

  private BigDecimal parseDecimal(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return new BigDecimal(raw.trim().replace(",", "."));
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private String buildNumeroDocumento(String estab, String ptoEmi, String secuencial) {
    if (secuencial == null || secuencial.isBlank()) {
      return null;
    }
    if (estab == null || estab.isBlank() || ptoEmi == null || ptoEmi.isBlank()) {
      return secuencial.trim();
    }
    return estab.trim() + "-" + ptoEmi.trim() + "-" + secuencial.trim();
  }

  private static final class ParsedDocumento {
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final String numeroAutorizacion;
    private final LocalDate fechaEmision;
    private final BigDecimal subtotal;
    private final BigDecimal impuestos;
    private final BigDecimal total;
    private final String moneda;
    private final String rucProveedor;
    private final String razonSocialProveedor;
    private final List<DocumentoProveedorItemCommand> items;

    private ParsedDocumento(
        String tipoDocumento,
        String numeroDocumento,
        String numeroAutorizacion,
        LocalDate fechaEmision,
        BigDecimal subtotal,
        BigDecimal impuestos,
        BigDecimal total,
        String moneda,
        String rucProveedor,
        String razonSocialProveedor,
        List<DocumentoProveedorItemCommand> items
    ) {
      this.tipoDocumento = Objects.requireNonNull(tipoDocumento, "tipoDocumento");
      this.numeroDocumento = numeroDocumento;
      this.numeroAutorizacion = numeroAutorizacion;
      this.fechaEmision = fechaEmision;
      this.subtotal = subtotal;
      this.impuestos = impuestos;
      this.total = total;
      this.moneda = moneda;
      this.rucProveedor = rucProveedor;
      this.razonSocialProveedor = razonSocialProveedor;
      this.items = items == null ? List.of() : List.copyOf(items);
    }
  }

  private void ensureBodegaExists(Long empresaId, Long bodegaId) {
    if (bodegaId == null) {
      return;
    }
    boolean existe = bodegaRepository.findByIdAndEmpresaId(bodegaId, empresaId).isPresent();
    if (!existe) {
      throw new ResourceNotFoundException("Bodega no encontrada");
    }
  }
}
