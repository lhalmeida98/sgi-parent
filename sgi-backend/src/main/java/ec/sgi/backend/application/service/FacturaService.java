package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.FacturaCreateResult;
import ec.sgi.backend.application.dto.FacturaEstadoResult;
import ec.sgi.backend.application.dto.FacturaProcesoResult;
import ec.sgi.backend.application.dto.FacturaTotalesDto;
import ec.sgi.backend.application.dto.SriConsultaEstadoRequest;
import ec.sgi.backend.application.dto.SriConsultaEstadoResult;
import ec.sgi.backend.application.dto.SriEmitirFacturaRequest;
import ec.sgi.backend.application.dto.SriEmitirFacturaResult;
import ec.sgi.backend.application.dto.SriEnvioStatus;
import ec.sgi.backend.application.dto.SriEstadoDto;
import ec.sgi.backend.application.dto.SriInfoTributariaDto;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.exception.SriCoreException;
import ec.sgi.backend.application.port.in.ConsultarEstadoFacturaCommand;
import ec.sgi.backend.application.port.in.ConsultarEstadoFacturaUseCase;
import ec.sgi.backend.application.port.in.ConsultarFacturaEnProcesoUseCase;
import ec.sgi.backend.application.port.in.CrearFacturaCommand;
import ec.sgi.backend.application.port.in.CrearFacturaUseCase;
import ec.sgi.backend.application.port.in.ListarFacturasEnProcesoUseCase;
import ec.sgi.backend.application.port.in.PagoFacturaCommand;
import ec.sgi.backend.application.port.in.ReenviarFacturasEnProcesoUseCase;
import ec.sgi.backend.application.port.out.ClienteRepository;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FacturaService implements CrearFacturaUseCase, ConsultarEstadoFacturaUseCase,
    ListarFacturasEnProcesoUseCase, ReenviarFacturasEnProcesoUseCase,
    ConsultarFacturaEnProcesoUseCase {
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
  private final SriCorePort sriCorePort;
  private final FacturaTotalsCalculator totalsCalculator;
  private final int maxIntentosConsulta;

  public FacturaService(
      FacturaRepository facturaRepository,
      ClienteRepository clienteRepository,
      ProductoRepository productoRepository,
      ImpuestoRepository impuestoRepository,
      InventarioRepository inventarioRepository,
      PreordenRepository preordenRepository,
      EmpresaRepository empresaRepository,
      FirmaElectronicaRepository firmaElectronicaRepository,
      SriCorePort sriCorePort,
      FacturaTotalsCalculator totalsCalculator,
      @Value("${app.facturas.sri.max-intentos:10}") int maxIntentosConsulta
  ) {
    this.facturaRepository = facturaRepository;
    this.clienteRepository = clienteRepository;
    this.productoRepository = productoRepository;
    this.impuestoRepository = impuestoRepository;
    this.inventarioRepository = inventarioRepository;
    this.preordenRepository = preordenRepository;
    this.empresaRepository = empresaRepository;
    this.firmaElectronicaRepository = firmaElectronicaRepository;
    this.sriCorePort = sriCorePort;
    this.totalsCalculator = totalsCalculator;
    this.maxIntentosConsulta = maxIntentosConsulta;
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
      Producto producto = productoRepository.findByIdAndEmpresaId(item.productoId(), empresa.id())
          .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
      Impuesto impuesto = impuestoRepository.findByIdAndEmpresaId(producto.impuestoId(), empresa.id())
          .orElseThrow(() -> new ResourceNotFoundException("Impuesto no encontrado"));
      items.add(new ItemCalculo(
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
        0,
        null
    );
    factura = facturaRepository.save(factura);

    ajustarInventarioPorFactura(calculo.items(), preorden, empresa.id());

    Empresa actualizada = empresa.withSecuencial(nextSecuencial(secuencial));
    empresaRepository.save(actualizada);

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
      facturaRepository.save(actualizadaFactura);
      return toCreateResult(actualizadaFactura);
    } catch (SriCoreException ex) {
      Factura error = factura
          .withEstado(FacturaEstado.ERROR)
          .withSriEstado(new SriEstado(null, null, ex.getMessage()));
      facturaRepository.save(error);
      throw ex;
    }
  }

  @Override
  public FacturaEstadoResult consultar(ConsultarEstadoFacturaCommand command) {
    Factura factura = facturaRepository.findById(command.facturaId())
        .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));
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
    facturaRepository.save(actualizada);
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
  public List<FacturaProcesoResult> reenviarEnProceso(Long empresaId) {
    List<Factura> pendientes = facturaRepository.findByEstadoAndEmpresaId(FacturaEstado.EN_PROCESO, empresaId);
    List<FacturaProcesoResult> resultados = new ArrayList<>();
    for (Factura factura : pendientes) {
      Factura actualizada = procesarConsultaFactura(factura);
      resultados.add(toProcesoResult(actualizada));
    }
    return resultados;
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
                || autorizacion.equals("RECHAZADA")) {
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
      return facturaRepository.save(sinClave);
    }

    if (factura.intentosConsulta() >= maxIntentosConsulta) {
      log.warn("Factura {} excede maximo de intentos ({})", factura.id(), maxIntentosConsulta);
      Factura agotada = factura
          .withEstado(FacturaEstado.ERROR)
          .withSriEstado(new SriEstado(null, null, "Maximo de intentos alcanzado"));
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
      return facturaRepository.save(actualizada);
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
      Inventario inventario = inventarioRepository.findByProductoIdAndEmpresaIdForUpdate(
          item.productoId(),
          empresaId
      )
          .orElseThrow(() -> new BusinessRuleException("Inventario no encontrado para producto " + item.productoId()));
      BigDecimal cantidad = item.cantidad();
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
    }
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
