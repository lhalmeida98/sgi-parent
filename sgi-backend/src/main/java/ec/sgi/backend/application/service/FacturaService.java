package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.FacturaCreateResult;
import ec.sgi.backend.application.dto.FacturaEstadoResult;
import ec.sgi.backend.application.dto.FacturaProcesoResult;
import ec.sgi.backend.application.dto.FacturaResumenPageResult;
import ec.sgi.backend.application.dto.FacturaResumenResult;
import ec.sgi.backend.application.dto.FacturaTotalesDto;
import ec.sgi.backend.application.dto.SriConsultaEstadoRequest;
import ec.sgi.backend.application.dto.SriConsultaEstadoResult;
import ec.sgi.backend.application.dto.SriEmitirFacturaRequest;
import ec.sgi.backend.application.dto.SriEmitirFacturaResult;
import ec.sgi.backend.application.dto.SriEnvioStatus;
import ec.sgi.backend.application.dto.SriEstadoDto;
import ec.sgi.backend.application.dto.SriInfoTributariaDto;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ForbiddenException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.exception.SriCoreException;
import ec.sgi.backend.application.port.in.ConsultarEstadoFacturaCommand;
import ec.sgi.backend.application.port.in.ConsultarEstadoFacturaUseCase;
import ec.sgi.backend.application.port.in.ConsultarFacturaEnProcesoUseCase;
import ec.sgi.backend.application.port.in.CrearFacturaCommand;
import ec.sgi.backend.application.port.in.CrearFacturaUseCase;
import ec.sgi.backend.application.port.in.ListarFacturasEnProcesoUseCase;
import ec.sgi.backend.application.port.in.ListarFacturasUseCase;
import ec.sgi.backend.application.port.in.ObtenerFacturaXmlUseCase;
import ec.sgi.backend.application.port.in.PagoFacturaCommand;
import ec.sgi.backend.application.port.in.ReenviarFacturasEnProcesoUseCase;
import ec.sgi.backend.application.port.in.ReenviarFacturaEnProcesoUseCase;
import ec.sgi.backend.application.usecase.EnviarFacturaPorEmailUseCase;
import ec.sgi.backend.application.port.out.ClienteRepository;
import ec.sgi.backend.application.port.out.BodegaRepository;
import ec.sgi.backend.application.port.out.EmpresaRepository;
import ec.sgi.backend.application.port.out.FacturaRepository;
import ec.sgi.backend.application.port.out.FirmaElectronicaRepository;
import ec.sgi.backend.application.port.out.ImpuestoRepository;
import ec.sgi.backend.application.port.out.InventarioRepository;
import ec.sgi.backend.application.port.out.PreordenRepository;
import ec.sgi.backend.application.port.out.ProductoRepository;
import ec.sgi.backend.application.port.out.SriCorePort;
import ec.sgi.backend.domain.model.Cliente;
import ec.sgi.backend.domain.model.Empresa;
import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.FacturaEstado;
import ec.sgi.backend.domain.model.FacturaItem;
import ec.sgi.backend.domain.model.FacturaPago;
import ec.sgi.backend.domain.model.Impuesto;
import ec.sgi.backend.domain.model.InfoTributariaData;
import ec.sgi.backend.domain.model.FirmaElectronica;
import ec.sgi.backend.domain.model.Inventario;
import ec.sgi.backend.domain.model.Preorden;
import ec.sgi.backend.domain.model.Producto;
import ec.sgi.backend.domain.model.SriEstado;
import ec.sgi.backend.domain.service.FacturaCalculoResult;
import ec.sgi.backend.domain.service.FacturaTotalsCalculator;
import ec.sgi.backend.domain.service.ItemCalculo;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FacturaService implements CrearFacturaUseCase, ConsultarEstadoFacturaUseCase,
    ListarFacturasEnProcesoUseCase, ReenviarFacturasEnProcesoUseCase,
    ReenviarFacturaEnProcesoUseCase, ConsultarFacturaEnProcesoUseCase,
    ListarFacturasUseCase, ObtenerFacturaXmlUseCase {
  private static final BigDecimal PROPINA_CERO = BigDecimal.ZERO.setScale(2);
  private static final Logger log = LoggerFactory.getLogger(FacturaService.class);

  private final FacturaRepository facturaRepository;
  private final ClienteRepository clienteRepository;
  private final ProductoRepository productoRepository;
  private final ImpuestoRepository impuestoRepository;
  private final InventarioRepository inventarioRepository;
  private final PreordenRepository preordenRepository;
  private final EmpresaRepository empresaRepository;
  private final FirmaElectronicaRepository firmaElectronicaRepository;
  private final BodegaRepository bodegaRepository;
  private final SriCorePort sriCorePort;
  private final FacturaTotalsCalculator totalsCalculator;
  private final int maxIntentosConsulta;
  private final EnviarFacturaPorEmailUseCase enviarFacturaPorEmailUseCase;
  private final boolean autoSendAuthorized;
  private final boolean storeXmlFirmado;
  private final boolean compressXmlFirmado;
  private final boolean compressXmlAutorizado;
  private final boolean consultaInmediataEnabled;
  private final long consultaInmediataDelayMs;

  public FacturaService(
      FacturaRepository facturaRepository,
      ClienteRepository clienteRepository,
      ProductoRepository productoRepository,
      ImpuestoRepository impuestoRepository,
      InventarioRepository inventarioRepository,
      PreordenRepository preordenRepository,
      EmpresaRepository empresaRepository,
      FirmaElectronicaRepository firmaElectronicaRepository,
      BodegaRepository bodegaRepository,
      SriCorePort sriCorePort,
      FacturaTotalsCalculator totalsCalculator,
      EnviarFacturaPorEmailUseCase enviarFacturaPorEmailUseCase,
      @Value("${app.facturas.sri.max-intentos:10}") int maxIntentosConsulta,
      @Value("${app.facturas.email.auto-send-authorized:true}") boolean autoSendAuthorized,
      @Value("${app.facturas.xml-firmado.store:true}") boolean storeXmlFirmado,
      @Value("${app.facturas.xml-firmado.compress:true}") boolean compressXmlFirmado,
      @Value("${app.facturas.xml-autorizado.compress:true}") boolean compressXmlAutorizado,
      @Value("${app.facturas.sri.consulta-inmediata.enabled:true}") boolean consultaInmediataEnabled,
      @Value("${app.facturas.sri.consulta-inmediata.delay-ms:10000}") long consultaInmediataDelayMs
  ) {
    this.facturaRepository = facturaRepository;
    this.clienteRepository = clienteRepository;
    this.productoRepository = productoRepository;
    this.impuestoRepository = impuestoRepository;
    this.inventarioRepository = inventarioRepository;
    this.preordenRepository = preordenRepository;
    this.empresaRepository = empresaRepository;
    this.firmaElectronicaRepository = firmaElectronicaRepository;
    this.bodegaRepository = bodegaRepository;
    this.sriCorePort = sriCorePort;
    this.totalsCalculator = totalsCalculator;
    this.enviarFacturaPorEmailUseCase = enviarFacturaPorEmailUseCase;
    this.maxIntentosConsulta = maxIntentosConsulta;
    this.autoSendAuthorized = autoSendAuthorized;
    this.storeXmlFirmado = storeXmlFirmado;
    this.compressXmlFirmado = compressXmlFirmado;
    this.compressXmlAutorizado = compressXmlAutorizado;
    this.consultaInmediataEnabled = consultaInmediataEnabled;
    this.consultaInmediataDelayMs = consultaInmediataDelayMs;
  }

  @Override
  @Transactional(noRollbackFor = SriCoreException.class)
  public FacturaCreateResult crear(CrearFacturaCommand command) {
    Empresa empresa = empresaRepository.findByIdForUpdate(command.empresaId())
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
    FirmaElectronica firma = firmaElectronicaRepository.findByEmpresaId(empresa.id())
        .orElseThrow(() -> new BusinessRuleException("Empresa sin firma electronica registrada"));

    Cliente cliente = clienteRepository.findByIdAndEmpresaId(command.clienteId(), empresa.id())
        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

    Preorden preorden = null;
    if (command.preordenId() != null) {
      preorden = preordenRepository.findByIdAndEmpresaId(command.preordenId(), empresa.id())
          .orElseThrow(() -> new ResourceNotFoundException("Preorden no encontrada"));
    }

    List<ItemCalculo> items = new ArrayList<>();
    for (var item : command.items()) {
      if (item.bodegaId() != null) {
        bodegaRepository.findByIdAndEmpresaId(item.bodegaId(), empresa.id())
            .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada"));
      }
      Producto producto = productoRepository.findByIdAndEmpresaId(item.productoId(), empresa.id())
          .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
      Impuesto impuesto = impuestoRepository.findByIdAndEmpresaId(producto.impuestoId(), empresa.id())
          .orElseThrow(() -> new ResourceNotFoundException("Impuesto no encontrado"));
      items.add(new ItemCalculo(
          item.bodegaId(),
          producto.id(),
          producto.codigo(),
          producto.descripcion(),
          item.cantidad(),
          producto.precioUnitario(),
          item.descuento(),
          impuesto.codigo(),
          impuesto.codigoPorcentaje(),
          impuesto.tarifa()
      ));
    }

    FacturaCalculoResult calculo = totalsCalculator.calcular(items);
    List<FacturaPago> pagos = mapPagos(command.pagos());
    validarPagos(pagos, calculo.totales().importeTotal());
    String secuencial = normalizeSecuencial(empresa.secuencial());
    InfoTributariaData infoTributaria = new InfoTributariaData(
        empresa.ambiente(),
        empresa.tipoEmision(),
        empresa.razonSocial(),
        empresa.nombreComercial(),
        empresa.ruc(),
        empresa.dirMatriz(),
        empresa.estab(),
        empresa.ptoEmi(),
        secuencial
    );

    Factura factura = new Factura(
        null,
        empresa.id(),
        cliente.id(),
        command.preordenId(),
        infoTributaria,
        command.fechaEmision(),
        command.dirEstablecimiento(),
        command.moneda(),
        calculo.items(),
        calculo.totales(),
        pagos,
        FacturaEstado.CREADA,
        null,
        null,
        null,
        null,
        null,
        null,
        0,
        null
    );
    factura = facturaRepository.save(factura);

    ajustarInventarioPorFactura(calculo.items(), preorden, empresa.id());

    try {
      Path firmaPath = resolveFirmaPath(firma);
      SriEmitirFacturaRequest sriRequest = SriEmitirFacturaRequestBuilder.build(
          command,
          cliente,
          calculo,
          factura,
          PROPINA_CERO,
          new SriInfoTributariaDto(
              empresa.ambiente(),
              empresa.tipoEmision(),
              empresa.razonSocial(),
              empresa.nombreComercial(),
              empresa.ruc(),
              empresa.dirMatriz(),
              empresa.estab(),
              empresa.ptoEmi(),
              secuencial,
              firmaPath.toString(),
              firma.clave()
          )
      );

      SriEmitirFacturaResult result = sriCorePort.emitirFactura(sriRequest);
      FacturaEstado nuevoEstado = mapEstadoEnvio(result);
      SriEstado sriEstado = toSriEstado(result);
      Factura actualizadaFactura = factura
          .withEstado(nuevoEstado)
          .withClaveAcceso(result.claveAcceso())
          .withCoreComprobanteId(result.comprobanteId())
          .withSriEstado(sriEstado)
          .withNumeroAutorizacion(result.numeroAutorizacion());
      actualizadaFactura = applyXmlFirmado(actualizadaFactura, result);
      facturaRepository.save(actualizadaFactura);
      revertirInventarioSiAplica(factura, actualizadaFactura);
      autoSendIfAuthorized(factura, actualizadaFactura);
      actualizadaFactura = consultarEstadoConDelaySiAplica(actualizadaFactura);
      if (actualizadaFactura.estado() != FacturaEstado.NO_AUTORIZADA
          && actualizadaFactura.estado() != FacturaEstado.ERROR) {
        Empresa actualizada = empresa.withSecuencial(nextSecuencial(secuencial));
        empresaRepository.save(actualizada);
      }
      return toCreateResult(actualizadaFactura);
    } catch (SriCoreException ex) {
      try {
        revertirInventarioPorFactura(calculo.items(), preorden, empresa.id());
      } catch (RuntimeException revertEx) {
        log.error("No se pudo revertir inventario para factura {}: {}", factura.id(), revertEx.getMessage());
      }
      Factura error = factura
          .withEstado(FacturaEstado.ERROR)
          .withSriEstado(new SriEstado(null, null, ex.getMessage()));
      facturaRepository.save(error);
      throw ex;
    }
  }

  @Override
  public FacturaEstadoResult consultar(ConsultarEstadoFacturaCommand command) {
    Factura factura = obtenerFacturaPorNumero(command.empresaId(), command.numeroFactura());
    if (factura.claveAcceso() == null || factura.claveAcceso().isBlank()) {
      throw new BusinessRuleException("Factura sin clave de acceso registrada");
    }

    SriConsultaEstadoResult sriResult = sriCorePort.consultarEstado(new SriConsultaEstadoRequest(
        factura.infoTributaria().ambiente(),
        factura.claveAcceso()
    ));

    FacturaEstado nuevoEstado = mapEstado(sriResult);
    Factura actualizada = factura
        .withEstado(nuevoEstado)
        .withSriEstado(new SriEstado(
            sriResult.estadoConsulta(),
            sriResult.estadoAutorizacion(),
            sriResult.mensaje()
        ));
    actualizada = applyXmlAutorizado(actualizada, sriResult);
    revertirInventarioSiAplica(factura, actualizada);
    facturaRepository.save(actualizada);
    autoSendIfAuthorized(factura, actualizada);
    return toEstadoResult(actualizada);
  }

  @Override
  @Transactional(readOnly = true)
  public List<FacturaProcesoResult> listarEnProceso(Long empresaId) {
    return facturaRepository.findByEstadoAndEmpresaId(FacturaEstado.EN_PROCESO, empresaId).stream()
        .map(this::toProcesoResult)
        .toList();
  }

  @Override
  public FacturaResumenPageResult listarPorEmpresa(Long empresaId, LocalDate fechaDesde, LocalDate fechaHasta,
      int page, int size) {
    LocalDate desde = fechaDesde == null ? LocalDate.now() : fechaDesde;
    LocalDate hasta = fechaHasta == null ? desde : fechaHasta;
    if (hasta.isBefore(desde)) {
      throw new BusinessRuleException("fechaHasta debe ser mayor o igual a fechaDesde");
    }
    PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaEmision").and(Sort.by("id")));
    Page<Factura> facturas = facturaRepository.findByEmpresaIdAndFechaEmisionBetween(empresaId, desde, hasta, pageable);

    List<Cliente> clientes = clienteRepository.findByEmpresaId(empresaId);
    Map<Long, Cliente> clientesPorId = clientes.stream()
        .collect(Collectors.toMap(Cliente::id, cliente -> cliente, (a, b) -> a));
    List<FacturaResumenResult> items = facturas.getContent().stream()
        .map(factura -> toResumen(factura, clientesPorId.get(factura.clienteId())))
        .toList();

    return new FacturaResumenPageResult(
        items,
        facturas.getNumber(),
        facturas.getSize(),
        facturas.getTotalElements(),
        facturas.getTotalPages()
    );
  }

  @Override
  public String obtenerXml(Long facturaId, Long empresaId) {
    Factura factura = facturaRepository.findById(facturaId)
        .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));
    if (!empresaId.equals(factura.empresaId())) {
      throw new ForbiddenException("Factura no pertenece a la empresa");
    }
    String xml = decodeXmlStored(factura.xmlAutorizado());
    if (xml == null || xml.isBlank()) {
      throw new ResourceNotFoundException("XML no disponible");
    }
    return xml;
  }

  @Override
  public List<FacturaProcesoResult> reenviarEnProceso(Long empresaId) {
    List<Factura> pendientes = facturaRepository.findByEstadoAndEmpresaId(FacturaEstado.EN_PROCESO, empresaId);
    List<FacturaProcesoResult> resultados = new ArrayList<>();
    for (Factura factura : pendientes) {
      Factura actualizada = procesarConsultaFactura(factura);
      resultados.add(toProcesoResult(actualizada));
    }
    return resultados;
  }

  @Override
  public FacturaProcesoResult reenviarEnProceso(Long facturaId, Long empresaId) {
    Factura factura = facturaRepository.findById(facturaId)
        .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));
    if (!empresaId.equals(factura.empresaId())) {
      throw new ForbiddenException("Factura no pertenece a la empresa");
    }
    if (factura.estado() != FacturaEstado.EN_PROCESO) {
      throw new BusinessRuleException("Factura no esta en proceso");
    }
    Factura actualizada = procesarConsultaFactura(factura);
    return toProcesoResult(actualizada);
  }

  public int procesarFacturasEnProceso() {
    List<Factura> pendientes = facturaRepository.findByEstado(FacturaEstado.EN_PROCESO);
    int procesadas = 0;
    for (Factura factura : pendientes) {
      procesarConsultaFactura(factura);
      procesadas++;
    }
    return procesadas;
  }

  @Override
  @Transactional(readOnly = true)
  public FacturaProcesoResult consultarEnProceso(Long facturaId) {
    Factura factura = facturaRepository.findById(facturaId)
        .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));
    if (factura.estado() != FacturaEstado.EN_PROCESO) {
      throw new BusinessRuleException("Factura no esta en proceso");
    }
    return toProcesoResult(factura);
  }

  private FacturaEstado mapEstadoEnvio(SriEmitirFacturaResult result) {
    SriEnvioStatus estadoSri = result.estadoSri();
    if (estadoSri == null) {
      return FacturaEstado.ENVIADA_SRI;
    }
    return switch (estadoSri) {
      case AUTORIZADO -> FacturaEstado.AUTORIZADA;
      case RECHAZADO -> FacturaEstado.NO_AUTORIZADA;
      case ERROR -> FacturaEstado.ERROR;
      case RECIBIDO, EN_PROCESO -> FacturaEstado.EN_PROCESO;
    };
  }

  private SriEstado toSriEstado(SriEmitirFacturaResult result) {
    String mensajeSri = result.mensajeSri();
    if (mensajeSri == null || mensajeSri.isBlank()) {
      return null;
    }
    return new SriEstado(null, null, mensajeSri);
  }

    private FacturaEstado mapEstado(SriConsultaEstadoResult sriResult) {

        String autorizacion = normalizeEstado(sriResult.estadoAutorizacion());
        String consulta = normalizeEstado(sriResult.estadoConsulta());

    if (autorizacion.equals("NO AUTORIZADO")
            || autorizacion.equals("NO_AUTORIZADO")
            || autorizacion.equals("RECHAZADO")
            || autorizacion.equals("RECHAZADA")
            || consulta.equals("NO AUTORIZADO")
            || consulta.equals("NO_AUTORIZADO")
            || consulta.equals("RECHAZADO")
            || consulta.equals("RECHAZADA")) {
            return FacturaEstado.NO_AUTORIZADA;
        }

        if (autorizacion.equals("AUTORIZADO")) {
            return FacturaEstado.AUTORIZADA;
        }

        if (autorizacion.equals("EN PROCESO")
                || autorizacion.equals("EN_PROCESAMIENTO")
                || consulta.equals("EN PROCESO")
                || consulta.equals("EN_PROCESAMIENTO")
                || consulta.equals("RECIBIDA")) {
            return FacturaEstado.EN_PROCESO;
        }

        return FacturaEstado.EN_PROCESO;
    }


    private String normalizeEstado(String estado) {
    if (estado == null) {
      return "";
    }
    return estado.trim().toUpperCase();
  }

  private FacturaCreateResult toCreateResult(Factura factura) {
    return new FacturaCreateResult(
        factura.id(),
        factura.estado().name(),
        factura.claveAcceso(),
        factura.coreComprobanteId(),
        new FacturaTotalesDto(
            factura.totales().totalSinImpuestos(),
            factura.totales().totalDescuento(),
            factura.totales().totalImpuestos(),
            factura.totales().importeTotal()
        ),
        toSriEstadoDto(factura.sriEstado())
    );
  }

  private FacturaEstadoResult toEstadoResult(Factura factura) {
    SriEstadoDto sriDto = toSriEstadoDto(factura.sriEstado());
    return new FacturaEstadoResult(
        factura.id(),
        factura.estado().name(),
        factura.claveAcceso(),
        factura.coreComprobanteId(),
        sriDto
    );
  }

  private FacturaResumenResult toResumen(Factura factura, Cliente cliente) {
    String numeroFactura = buildNumeroFactura(factura.infoTributaria());
    return new FacturaResumenResult(
        factura.id(),
        factura.empresaId(),
        factura.clienteId(),
        cliente == null ? "" : cliente.razonSocial(),
        factura.fechaEmision(),
        numeroFactura,
        factura.estado().name(),
        factura.claveAcceso(),
        factura.numeroAutorizacion(),
        factura.totales().totalSinImpuestos(),
        factura.totales().totalDescuento(),
        factura.totales().totalImpuestos(),
        factura.totales().importeTotal()
    );
  }

  private String buildNumeroFactura(InfoTributariaData info) {
    if (info == null) {
      return "";
    }
    String estab = info.estab() == null ? "" : info.estab();
    String ptoEmi = info.ptoEmi() == null ? "" : info.ptoEmi();
    String secuencial = info.secuencial() == null ? "" : info.secuencial();
    return estab + "-" + ptoEmi + "-" + secuencial;
  }

  private Factura obtenerFacturaPorNumero(Long empresaId, String numeroFactura) {
    String valor = numeroFactura == null ? "" : numeroFactura.trim();
    if (valor.isEmpty()) {
      throw new BusinessRuleException("Numero de factura requerido");
    }
    if (valor.contains("-")) {
      String[] partes = valor.split("-");
      if (partes.length != 3) {
        throw new BusinessRuleException("Numero de factura invalido");
      }
      String estab = partes[0].trim();
      String ptoEmi = partes[1].trim();
      String secuencial = padSecuencial(empresaId, partes[2].trim());
      return facturaRepository.findByEmpresaIdAndInfoEstabAndInfoPtoEmiAndInfoSecuencial(
          empresaId, estab, ptoEmi, secuencial)
          .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));
    }
    String secuencial = padSecuencial(empresaId, valor);
    return facturaRepository.findByEmpresaIdAndInfoSecuencial(empresaId, secuencial)
        .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));
  }

  private String padSecuencial(Long empresaId, String secuencialInput) {
    String trimmed = secuencialInput == null ? "" : secuencialInput.trim();
    if (trimmed.isEmpty()) {
      return trimmed;
    }
    boolean numeric = trimmed.chars().allMatch(Character::isDigit);
    if (!numeric) {
      return trimmed;
    }
    int targetLength = empresaRepository.findById(empresaId)
        .map(Empresa::secuencial)
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .map(String::length)
        .orElse(trimmed.length());
    if (trimmed.length() >= targetLength) {
      return trimmed;
    }
    return "0".repeat(targetLength - trimmed.length()) + trimmed;
  }

  private FacturaProcesoResult toProcesoResult(Factura factura) {
    return new FacturaProcesoResult(
        factura.id(),
        factura.estado().name(),
        factura.claveAcceso(),
        factura.coreComprobanteId(),
        factura.intentosConsulta(),
        factura.ultimoIntentoConsulta(),
        factura.numeroAutorizacion(),
        toSriEstadoDto(factura.sriEstado())
    );
  }

  private SriEstadoDto toSriEstadoDto(SriEstado sri) {
    if (sri == null) {
      return new SriEstadoDto(null, null, null);
    }
    return new SriEstadoDto(sri.estadoConsulta(), sri.estadoAutorizacion(), sri.mensaje());
  }

  private Factura procesarConsultaFactura(Factura factura) {
    if (factura.claveAcceso() == null || factura.claveAcceso().isBlank()) {
      log.warn("Factura {} sin clave de acceso, se marca como ERROR", factura.id());
      Factura sinClave = factura
          .withEstado(FacturaEstado.ERROR)
          .withSriEstado(new SriEstado(null, null, "Factura sin clave de acceso"));
      revertirInventarioSiAplica(factura, sinClave);
      return facturaRepository.save(sinClave);
    }

    if (factura.intentosConsulta() >= maxIntentosConsulta) {
      log.warn("Factura {} excede maximo de intentos ({})", factura.id(), maxIntentosConsulta);
      Factura agotada = factura
          .withEstado(FacturaEstado.ERROR)
          .withSriEstado(new SriEstado(null, null, "Maximo de intentos alcanzado"));
      revertirInventarioSiAplica(factura, agotada);
      return facturaRepository.save(agotada);
    }

    LocalDateTime ahora = LocalDateTime.now();
    Factura intento = factura.withIntentoConsulta(factura.intentosConsulta() + 1, ahora);
    facturaRepository.save(intento);

    try {
      SriConsultaEstadoResult sriResult = sriCorePort.consultarEstado(new SriConsultaEstadoRequest(
          factura.infoTributaria().ambiente(),
          factura.claveAcceso()
      ));
      FacturaEstado nuevoEstado = mapEstado(sriResult);
      Factura actualizada = intento
          .withEstado(nuevoEstado)
          .withSriEstado(new SriEstado(
              sriResult.estadoConsulta(),
              sriResult.estadoAutorizacion(),
              sriResult.mensaje()
          ));
      if (actualizada.claveAcceso() == null || actualizada.claveAcceso().isBlank()) {
        actualizada = actualizada.withClaveAcceso(sriResult.claveAcceso());
      }
      actualizada = applyXmlAutorizado(actualizada, sriResult);
      revertirInventarioSiAplica(factura, actualizada);
      Factura guardada = facturaRepository.save(actualizada);
      autoSendIfAuthorized(intento, guardada);
      return guardada;
    } catch (SriCoreException ex) {
      log.warn("Error consultando estado SRI para factura {}: {}", factura.id(), ex.getMessage());
      Factura error = intento.withSriEstado(new SriEstado(null, null, ex.getMessage()));
      return facturaRepository.save(error);
    } catch (RuntimeException ex) {
      log.error("Error inesperado consultando estado SRI para factura {}", factura.id(), ex);
      Factura error = intento.withSriEstado(new SriEstado(null, null, ex.getMessage()));
      return facturaRepository.save(error);
    }
  }

  private void ajustarInventarioPorFactura(List<FacturaItem> items, Preorden preorden, Long empresaId) {
    boolean usaReserva = preorden != null && preorden.reservaInventario();
    LocalDateTime ahora = LocalDateTime.now();
    for (FacturaItem item : items) {
      BigDecimal cantidad = item.cantidad();
      if (item.bodegaId() != null) {
        Inventario inventario = inventarioRepository.findByProductoIdAndEmpresaIdAndBodegaIdForUpdate(
            item.productoId(),
            empresaId,
            item.bodegaId()
        ).orElseThrow(() -> new BusinessRuleException("Inventario no encontrado para producto " + item.productoId()));
        if (usaReserva) {
          if (inventario.stockReservado().compareTo(cantidad) < 0) {
            throw new BusinessRuleException("Stock reservado insuficiente para producto " + item.productoId());
          }
          if (inventario.stockActual().compareTo(cantidad) < 0) {
            throw new BusinessRuleException("Stock insuficiente para facturar producto " + item.productoId());
          }
          Inventario actualizado = inventario
              .withStockActual(inventario.stockActual().subtract(cantidad))
              .withStockReservado(inventario.stockReservado().subtract(cantidad))
              .withActualizadoEn(ahora);
          inventarioRepository.save(actualizado);
        } else {
          BigDecimal disponible = inventario.stockActual().subtract(inventario.stockReservado());
          if (disponible.compareTo(cantidad) < 0) {
            throw new BusinessRuleException("Stock insuficiente para facturar producto " + item.productoId());
          }
          Inventario actualizado = inventario
              .withStockActual(inventario.stockActual().subtract(cantidad))
              .withActualizadoEn(ahora);
          inventarioRepository.save(actualizado);
        }
        continue;
      }
      List<Inventario> inventarios = inventarioRepository.findByProductoIdAndEmpresaId(
          item.productoId(),
          empresaId
      );
      if (inventarios.isEmpty()) {
        throw new BusinessRuleException("Inventario no encontrado para producto " + item.productoId());
      }
      if (usaReserva) {
        BigDecimal reservadoTotal = inventarios.stream()
            .map(Inventario::stockReservado)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal stockTotal = inventarios.stream()
            .map(Inventario::stockActual)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (reservadoTotal.compareTo(cantidad) < 0) {
          throw new BusinessRuleException("Stock reservado insuficiente para producto " + item.productoId());
        }
        if (stockTotal.compareTo(cantidad) < 0) {
          throw new BusinessRuleException("Stock insuficiente para facturar producto " + item.productoId());
        }
        descontarConReserva(inventarios, cantidad, ahora);
      } else {
        BigDecimal disponibleTotal = inventarios.stream()
            .map(inv -> inv.stockActual().subtract(inv.stockReservado()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (disponibleTotal.compareTo(cantidad) < 0) {
          throw new BusinessRuleException("Stock insuficiente para facturar producto " + item.productoId());
        }
        descontarSinReserva(inventarios, cantidad, ahora);
      }
    }
  }

  private void revertirInventarioSiAplica(Factura anterior, Factura actualizada) {
    if (anterior == null || actualizada == null) {
      return;
    }
    if (!requiereReversion(anterior.estado(), actualizada.estado())) {
      return;
    }
    Preorden preorden = obtenerPreordenParaFactura(actualizada);
    revertirInventarioPorFactura(actualizada.items(), preorden, actualizada.empresaId());
  }

  private boolean requiereReversion(FacturaEstado anterior, FacturaEstado nuevo) {
    if (nuevo != FacturaEstado.NO_AUTORIZADA && nuevo != FacturaEstado.ERROR) {
      return false;
    }
    return anterior != FacturaEstado.NO_AUTORIZADA && anterior != FacturaEstado.ERROR;
  }

  private Preorden obtenerPreordenParaFactura(Factura factura) {
    if (factura == null || factura.preordenId() == null) {
      return null;
    }
    return preordenRepository.findByIdAndEmpresaId(factura.preordenId(), factura.empresaId())
        .orElse(null);
  }

  private void revertirInventarioPorFactura(List<FacturaItem> items, Preorden preorden, Long empresaId) {
    boolean usaReserva = preorden != null && preorden.reservaInventario();
    LocalDateTime ahora = LocalDateTime.now();
    for (FacturaItem item : items) {
      BigDecimal cantidad = item.cantidad();
      if (item.bodegaId() != null) {
        Inventario inventario = inventarioRepository.findByProductoIdAndEmpresaIdAndBodegaIdForUpdate(
            item.productoId(),
            empresaId,
            item.bodegaId()
        ).orElseThrow(() -> new BusinessRuleException("Inventario no encontrado para producto " + item.productoId()));
        if (usaReserva) {
          Inventario actualizado = inventario
              .withStockActual(inventario.stockActual().add(cantidad))
              .withStockReservado(inventario.stockReservado().add(cantidad))
              .withActualizadoEn(ahora);
          inventarioRepository.save(actualizado);
        } else {
          Inventario actualizado = inventario
              .withStockActual(inventario.stockActual().add(cantidad))
              .withActualizadoEn(ahora);
          inventarioRepository.save(actualizado);
        }
        continue;
      }
      List<Inventario> inventarios = inventarioRepository.findByProductoIdAndEmpresaId(
          item.productoId(),
          empresaId
      );
      if (inventarios.isEmpty()) {
        throw new BusinessRuleException("Inventario no encontrado para producto " + item.productoId());
      }
      if (usaReserva) {
        restaurarConReserva(inventarios, cantidad, ahora);
      } else {
        restaurarSinReserva(inventarios, cantidad, ahora);
      }
    }
  }

  private void restaurarConReserva(List<Inventario> inventarios, BigDecimal cantidad, LocalDateTime ahora) {
    BigDecimal restante = cantidad;
    List<Inventario> ordenados = inventarios.stream()
        .sorted((a, b) -> b.stockReservado().compareTo(a.stockReservado()))
        .toList();
    for (int i = 0; i < ordenados.size(); i++) {
      if (restante.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
      int restantes = ordenados.size() - i;
      BigDecimal tomar = restantes == 1
          ? restante
          : restante.divide(BigDecimal.valueOf(restantes), 4, RoundingMode.HALF_UP);
      if (tomar.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      Inventario inventario = ordenados.get(i);
      Inventario actualizado = inventario
          .withStockActual(inventario.stockActual().add(tomar))
          .withStockReservado(inventario.stockReservado().add(tomar))
          .withActualizadoEn(ahora);
      inventarioRepository.save(actualizado);
      restante = restante.subtract(tomar);
    }
  }

  private void restaurarSinReserva(List<Inventario> inventarios, BigDecimal cantidad, LocalDateTime ahora) {
    BigDecimal restante = cantidad;
    List<Inventario> ordenados = inventarios.stream()
        .sorted((a, b) -> {
          BigDecimal disponibleA = a.stockActual().subtract(a.stockReservado());
          BigDecimal disponibleB = b.stockActual().subtract(b.stockReservado());
          return disponibleB.compareTo(disponibleA);
        })
        .toList();
    for (int i = 0; i < ordenados.size(); i++) {
      if (restante.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
      int restantes = ordenados.size() - i;
      BigDecimal tomar = restantes == 1
          ? restante
          : restante.divide(BigDecimal.valueOf(restantes), 4, RoundingMode.HALF_UP);
      if (tomar.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      Inventario inventario = ordenados.get(i);
      Inventario actualizado = inventario
          .withStockActual(inventario.stockActual().add(tomar))
          .withActualizadoEn(ahora);
      inventarioRepository.save(actualizado);
      restante = restante.subtract(tomar);
    }
  }

  private void descontarConReserva(List<Inventario> inventarios, BigDecimal cantidad, LocalDateTime ahora) {
    BigDecimal restante = cantidad;
    List<Inventario> ordenados = inventarios.stream()
        .sorted((a, b) -> b.stockReservado().compareTo(a.stockReservado()))
        .toList();
    for (Inventario inventario : ordenados) {
      if (restante.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
      BigDecimal reservado = inventario.stockReservado();
      if (reservado.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      BigDecimal tomar = reservado.min(restante);
      Inventario actualizado = inventario
          .withStockActual(inventario.stockActual().subtract(tomar))
          .withStockReservado(inventario.stockReservado().subtract(tomar))
          .withActualizadoEn(ahora);
      inventarioRepository.save(actualizado);
      restante = restante.subtract(tomar);
    }
    if (restante.compareTo(BigDecimal.ZERO) > 0) {
      throw new BusinessRuleException("No se pudo descontar todo el stock reservado");
    }
  }

  private void descontarSinReserva(List<Inventario> inventarios, BigDecimal cantidad, LocalDateTime ahora) {
    BigDecimal restante = cantidad;
    List<Inventario> ordenados = inventarios.stream()
        .sorted((a, b) -> {
          BigDecimal disponibleA = a.stockActual().subtract(a.stockReservado());
          BigDecimal disponibleB = b.stockActual().subtract(b.stockReservado());
          return disponibleB.compareTo(disponibleA);
        })
        .toList();
    for (Inventario inventario : ordenados) {
      if (restante.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
      BigDecimal disponible = inventario.stockActual().subtract(inventario.stockReservado());
      if (disponible.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      BigDecimal tomar = disponible.min(restante);
      Inventario actualizado = inventario
          .withStockActual(inventario.stockActual().subtract(tomar))
          .withActualizadoEn(ahora);
      inventarioRepository.save(actualizado);
      restante = restante.subtract(tomar);
    }
    if (restante.compareTo(BigDecimal.ZERO) > 0) {
      throw new BusinessRuleException("No se pudo descontar todo el stock disponible");
    }
  }

  private void autoSendIfAuthorized(Factura anterior, Factura actualizada) {
    if (!autoSendAuthorized || actualizada == null) {
      return;
    }
    if (actualizada.estado() != FacturaEstado.AUTORIZADA) {
      return;
    }
    if (anterior != null && anterior.estado() == FacturaEstado.AUTORIZADA) {
      return;
    }
    if (isBlank(actualizada.xmlAutorizado())) {
      log.warn("Factura {} autorizada sin XML; se omite envio automatico.", actualizada.id());
      return;
    }
    try {
      enviarFacturaPorEmailUseCase.execute(actualizada.id(), actualizada.empresaId(), null);
    } catch (BusinessRuleException ex) {
      log.warn("No se pudo enviar email automatico para factura {}: {}", actualizada.id(), ex.getMessage());
    } catch (RuntimeException ex) {
      log.error("Error enviando email automatico para factura {}", actualizada.id(), ex);
    }
  }

  private Factura applyXmlFirmado(Factura factura, SriEmitirFacturaResult result) {
    if (factura == null || result == null || !storeXmlFirmado) {
      return factura;
    }
    String xmlFirmado = result.xmlFirmado();
    if (isBlank(xmlFirmado)) {
      return factura;
    }
    String stored = compressXmlFirmado ? gzipBase64(xmlFirmado) : xmlFirmado;
    return factura.withXmlFirmado(stored);
  }

  private Factura applyXmlAutorizado(Factura factura, SriConsultaEstadoResult sriResult) {
    if (factura == null || sriResult == null) {
      return factura;
    }
    if (factura.estado() != FacturaEstado.AUTORIZADA) {
      return factura;
    }
    if (!isBlank(factura.xmlAutorizado())) {
      if (!isBlank(factura.xmlFirmado())) {
        return factura.withXmlFirmado(null);
      }
      return factura;
    }
    String xml = sriResult.xmlAutorizado();
    if (isBlank(xml)) {
      xml = decodeXmlStored(factura.xmlFirmado());
    }
    if (isBlank(xml)) {
      return factura;
    }
    String stored = compressXmlAutorizado ? gzipBase64(xml) : xml;
    return factura.withXmlAutorizado(stored).withXmlFirmado(null);
  }

  private Factura consultarEstadoConDelaySiAplica(Factura factura) {
    if (factura == null || !consultaInmediataEnabled) {
      return factura;
    }
    if (factura.estado() != FacturaEstado.EN_PROCESO) {
      return factura;
    }
    if (consultaInmediataDelayMs <= 0) {
      return procesarConsultaFactura(factura);
    }
    try {
      Thread.sleep(consultaInmediataDelayMs);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return factura;
    }
    return procesarConsultaFactura(factura);
  }

  private String gzipBase64(String value) {
    try {
      byte[] input = value.getBytes(StandardCharsets.UTF_8);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
        gzip.write(input);
      }
      return "gzip:" + Base64.getEncoder().encodeToString(out.toByteArray());
    } catch (IOException ex) {
      log.warn("No se pudo comprimir XML firmado, se guarda sin comprimir.");
      return value;
    }
  }

  private String decodeXmlStored(String value) {
    if (isBlank(value)) {
      return null;
    }
    if (!value.startsWith("gzip:")) {
      return value;
    }
    String base64 = value.substring("gzip:".length());
    try {
      byte[] compressed = Base64.getDecoder().decode(base64);
      try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
        return new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
      }
    } catch (IOException | IllegalArgumentException ex) {
      log.warn("No se pudo descomprimir XML almacenado.");
      return null;
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private List<FacturaPago> mapPagos(List<PagoFacturaCommand> pagos) {
    return pagos.stream()
        .map(pago -> new FacturaPago(pago.formaPago(), pago.monto()))
        .toList();
  }

  private void validarPagos(List<FacturaPago> pagos, BigDecimal importeTotal) {
    if (pagos.isEmpty() || pagos.size() > 2) {
      throw new BusinessRuleException("La factura debe tener uno o dos pagos");
    }
    BigDecimal totalPagos = BigDecimal.ZERO;
    for (FacturaPago pago : pagos) {
      totalPagos = totalPagos.add(pago.monto());
    }
    if (totalPagos.compareTo(importeTotal) != 0) {
      throw new BusinessRuleException("La suma de pagos debe ser igual al total de la factura");
    }
  }

  private String normalizeSecuencial(String secuencial) {
    String trimmed = secuencial == null ? "" : secuencial.trim();
    if (trimmed.isEmpty()) {
      throw new BusinessRuleException("Secuencial de empresa no configurado");
    }
    for (int i = 0; i < trimmed.length(); i++) {
      char ch = trimmed.charAt(i);
      if (ch < '0' || ch > '9') {
        throw new BusinessRuleException("Secuencial de empresa debe ser numerico");
      }
    }
    return trimmed;
  }

  private String nextSecuencial(String secuencial) {
    String normalized = normalizeSecuencial(secuencial);
    BigInteger next = new BigInteger(normalized).add(BigInteger.ONE);
    String nextValue = next.toString();
    if (nextValue.length() < normalized.length()) {
      return "0".repeat(normalized.length() - nextValue.length()) + nextValue;
    }
    return nextValue;
  }

  private Path resolveFirmaPath(FirmaElectronica firma) {
    String rutaArchivo = firma.rutaArchivo();
    if (rutaArchivo == null || rutaArchivo.isBlank()) {
      throw new BusinessRuleException("Ruta de firma electronica no configurada");
    }
    Path path = Path.of(rutaArchivo);
    if (!Files.exists(path)) {
      throw new BusinessRuleException("No se encuentra el archivo de firma");
    }
    return path;
  }
}
