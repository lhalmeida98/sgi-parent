package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.DashboardFacturaItemResult;
import ec.sgi.backend.application.dto.DashboardFlujoCajaResult;
import ec.sgi.backend.application.dto.DashboardProductoVendidoResult;
import ec.sgi.backend.application.dto.DashboardProductoStockBajoResult;
import ec.sgi.backend.application.dto.DashboardResumenResult;
import ec.sgi.backend.application.port.in.ObtenerDashboardResumenUseCase;
import ec.sgi.backend.application.port.out.CobroClienteRepository;
import ec.sgi.backend.application.port.out.CuentaPorCobrarRepository;
import ec.sgi.backend.application.port.out.CuentaPorPagarRepository;
import ec.sgi.backend.application.port.out.FacturaRepository;
import ec.sgi.backend.application.port.out.InventarioRepository;
import ec.sgi.backend.application.port.out.PagoProveedorRepository;
import ec.sgi.backend.application.port.out.ProductoRepository;
import ec.sgi.backend.domain.model.CobroCliente;
import ec.sgi.backend.domain.model.CuentaPorCobrar;
import ec.sgi.backend.domain.model.CuentaPorPagar;
import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.FacturaEstado;
import ec.sgi.backend.domain.model.FacturaItem;
import ec.sgi.backend.domain.model.InfoTributariaData;
import ec.sgi.backend.domain.model.Inventario;
import ec.sgi.backend.domain.model.PagoProveedor;
import ec.sgi.backend.domain.model.Producto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService implements ObtenerDashboardResumenUseCase {
  private static final String ESTADO_COBRADA = "COBRADA";
  private static final String ESTADO_ANULADA = "ANULADA";
  private static final String ESTADO_PAGADO = "PAGADO";

  private final FacturaRepository facturaRepository;
  private final CuentaPorCobrarRepository cuentaPorCobrarRepository;
  private final CuentaPorPagarRepository cuentaPorPagarRepository;
  private final InventarioRepository inventarioRepository;
  private final CobroClienteRepository cobroClienteRepository;
  private final PagoProveedorRepository pagoProveedorRepository;
  private final ProductoRepository productoRepository;

  public DashboardService(
      FacturaRepository facturaRepository,
      CuentaPorCobrarRepository cuentaPorCobrarRepository,
      CuentaPorPagarRepository cuentaPorPagarRepository,
      InventarioRepository inventarioRepository,
      CobroClienteRepository cobroClienteRepository,
      PagoProveedorRepository pagoProveedorRepository,
      ProductoRepository productoRepository
  ) {
    this.facturaRepository = facturaRepository;
    this.cuentaPorCobrarRepository = cuentaPorCobrarRepository;
    this.cuentaPorPagarRepository = cuentaPorPagarRepository;
    this.inventarioRepository = inventarioRepository;
    this.cobroClienteRepository = cobroClienteRepository;
    this.pagoProveedorRepository = pagoProveedorRepository;
    this.productoRepository = productoRepository;
  }

  @Override
  public DashboardResumenResult resumen(Long empresaId) {
    LocalDate hoy = LocalDate.now();
    LocalDate inicioMes = hoy.withDayOfMonth(1);
    LocalDate inicioMesAnterior = inicioMes.minusMonths(1);
    LocalDate finMesAnterior = inicioMes.minusDays(1);

    BigDecimal ventasMes = calcularVentas(empresaId, inicioMes, hoy);
    BigDecimal ventasMesAnterior = calcularVentas(empresaId, inicioMesAnterior, finMesAnterior);
    BigDecimal variacion = calcularVariacionPct(ventasMes, ventasMesAnterior);

    List<CuentaPorCobrar> cxc = cuentaPorCobrarRepository.findByEmpresaId(empresaId);
    BigDecimal saldoCxc = cxc.stream()
        .filter(cuenta -> cuenta.saldo() != null && cuenta.saldo().compareTo(BigDecimal.ZERO) > 0)
        .filter(cuenta -> !ESTADO_COBRADA.equals(cuenta.estado()) && !ESTADO_ANULADA.equals(cuenta.estado()))
        .map(CuentaPorCobrar::saldo)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    long pendientesHoy = cxc.stream()
        .filter(cuenta -> cuenta.saldo() != null && cuenta.saldo().compareTo(BigDecimal.ZERO) > 0)
        .filter(cuenta -> cuenta.fechaVencimiento() != null && cuenta.fechaVencimiento().equals(hoy))
        .count();

    long stockCritico = calcularStockCritico(empresaId);

    List<CuentaPorPagar> cxp = cuentaPorPagarRepository.findByEmpresaId(empresaId);
    BigDecimal saldoCxp = cxp.stream()
        .filter(cuenta -> cuenta.saldo() != null && cuenta.saldo().compareTo(BigDecimal.ZERO) > 0)
        .filter(cuenta -> !ESTADO_PAGADO.equals(cuenta.estado()))
        .map(CuentaPorPagar::saldo)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    long vencenSemana = cxp.stream()
        .filter(cuenta -> cuenta.saldo() != null && cuenta.saldo().compareTo(BigDecimal.ZERO) > 0)
        .filter(cuenta -> cuenta.fechaVencimiento() != null)
        .filter(cuenta -> diasEntre(hoy, cuenta.fechaVencimiento()) >= 0
            && diasEntre(hoy, cuenta.fechaVencimiento()) <= 7)
        .count();

    List<DashboardFlujoCajaResult> flujoCaja = buildFlujoCaja30Dias(empresaId, hoy);
    List<DashboardFacturaItemResult> ultimasFacturas = buildUltimasFacturas(empresaId, hoy);
    List<DashboardProductoVendidoResult> productosMasVendidos = buildProductosMasVendidos(empresaId, hoy);
    List<DashboardProductoStockBajoResult> productosMenosStock = buildProductosMenosStock(empresaId);

    return new DashboardResumenResult(
        ventasMes,
        ventasMesAnterior,
        variacion,
        saldoCxc,
        pendientesHoy,
        stockCritico,
        saldoCxp,
        vencenSemana,
        flujoCaja,
        ultimasFacturas,
        productosMasVendidos,
        productosMenosStock
    );
  }

  private BigDecimal calcularVentas(Long empresaId, LocalDate desde, LocalDate hasta) {
    if (desde == null || hasta == null) {
      return BigDecimal.ZERO;
    }
    List<Factura> facturas = fetchFacturas(empresaId, desde, hasta, 500);
    return facturas.stream()
        .filter(factura -> factura.estado() == FacturaEstado.AUTORIZADA)
        .map(factura -> factura.totales().importeTotal())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal calcularVariacionPct(BigDecimal actual, BigDecimal anterior) {
    if (anterior == null || anterior.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    if (actual == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal diferencia = actual.subtract(anterior);
    return diferencia
        .divide(anterior, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .setScale(2, RoundingMode.HALF_UP);
  }

  private long calcularStockCritico(Long empresaId) {
    List<Inventario> inventarios = inventarioRepository.findByEmpresaId(empresaId);
    return inventarios.stream()
        .filter(inv -> inv.stockMinimo() != null)
        .filter(inv -> inv.stockActual().compareTo(inv.stockMinimo()) <= 0)
        .count();
  }

  private List<DashboardFlujoCajaResult> buildFlujoCaja30Dias(Long empresaId, LocalDate hoy) {
    LocalDate inicio = hoy.minusDays(29);
    Map<LocalDate, BigDecimal> ingresos = new HashMap<>();
    Map<LocalDate, BigDecimal> egresos = new HashMap<>();
    for (int i = 0; i < 30; i++) {
      LocalDate fecha = inicio.plusDays(i);
      ingresos.put(fecha, BigDecimal.ZERO);
      egresos.put(fecha, BigDecimal.ZERO);
    }

    for (CobroCliente cobro : cobroClienteRepository.findByEmpresaId(empresaId)) {
      if (cobro.fecha() == null || cobro.montoTotal() == null) {
        continue;
      }
      if (cobro.fecha().isBefore(inicio) || cobro.fecha().isAfter(hoy)) {
        continue;
      }
      ingresos.put(cobro.fecha(), ingresos.get(cobro.fecha()).add(cobro.montoTotal()));
    }

    for (PagoProveedor pago : pagoProveedorRepository.findByEmpresaId(empresaId)) {
      if (pago.fechaPago() == null || pago.montoTotal() == null) {
        continue;
      }
      if (pago.fechaPago().isBefore(inicio) || pago.fechaPago().isAfter(hoy)) {
        continue;
      }
      egresos.put(pago.fechaPago(), egresos.get(pago.fechaPago()).add(pago.montoTotal()));
    }

    List<DashboardFlujoCajaResult> resultados = new ArrayList<>();
    for (int i = 0; i < 30; i++) {
      LocalDate fecha = inicio.plusDays(i);
      BigDecimal in = ingresos.get(fecha);
      BigDecimal out = egresos.get(fecha);
      resultados.add(new DashboardFlujoCajaResult(
          fecha,
          in,
          out,
          in.subtract(out)
      ));
    }
    return resultados;
  }

  private List<DashboardFacturaItemResult> buildUltimasFacturas(Long empresaId, LocalDate hoy) {
    LocalDate desde = hoy.minusDays(30);
    PageRequest pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "fechaEmision")
        .and(Sort.by(Sort.Direction.DESC, "id")));
    Page<Factura> page = facturaRepository.findByEmpresaIdAndFechaEmisionBetween(empresaId, desde, hoy, pageable);
    return page.getContent().stream()
        .map(this::toFacturaItem)
        .toList();
  }

  private List<DashboardProductoVendidoResult> buildProductosMasVendidos(Long empresaId, LocalDate hoy) {
    LocalDate desde = hoy.minusDays(30);
    List<Factura> facturas = fetchFacturas(empresaId, desde, hoy, 500).stream()
        .filter(factura -> factura.estado() == FacturaEstado.AUTORIZADA)
        .toList();

    Map<Long, ProductoResumen> resumenes = new HashMap<>();
    for (Factura factura : facturas) {
      for (FacturaItem item : factura.items()) {
        ProductoResumen resumen = resumenes.computeIfAbsent(item.productoId(), id -> new ProductoResumen(id,
            item.descripcion()));
        resumen.cantidad = resumen.cantidad.add(item.cantidad());
        resumen.total = resumen.total.add(item.precioTotalSinImpuesto());
      }
    }

    return resumenes.values().stream()
        .sorted(Comparator.comparing(ProductoResumen::getTotal).reversed())
        .limit(5)
        .map(resumen -> new DashboardProductoVendidoResult(
            resumen.productoId,
            resumen.descripcion,
            resumen.cantidad,
            resumen.total
        ))
        .toList();
  }

  private List<DashboardProductoStockBajoResult> buildProductosMenosStock(Long empresaId) {
    List<Inventario> inventarios = inventarioRepository.findByEmpresaId(empresaId);
    Map<Long, StockResumen> resumenes = new HashMap<>();
    for (Inventario inventario : inventarios) {
      StockResumen resumen = resumenes.computeIfAbsent(inventario.productoId(),
          id -> new StockResumen(id));
      BigDecimal stock = inventario.stockActual() == null ? BigDecimal.ZERO : inventario.stockActual();
      BigDecimal minimo = inventario.stockMinimo() == null ? BigDecimal.ZERO : inventario.stockMinimo();
      resumen.stockActual = resumen.stockActual.add(stock);
      resumen.stockMinimo = resumen.stockMinimo.add(minimo);
    }
    Map<Long, String> descripciones = new HashMap<>();
    for (Producto producto : productoRepository.findByEmpresaId(empresaId)) {
      descripciones.put(producto.id(), producto.descripcion());
    }
    return resumenes.values().stream()
        .sorted(Comparator.comparing(StockResumen::getStockActual))
        .limit(5)
        .map(resumen -> new DashboardProductoStockBajoResult(
            resumen.productoId,
            descripciones.getOrDefault(resumen.productoId, ""),
            resumen.stockActual,
            resumen.stockMinimo
        ))
        .toList();
  }

  private DashboardFacturaItemResult toFacturaItem(Factura factura) {
    return new DashboardFacturaItemResult(
        factura.id(),
        buildNumeroFactura(factura.infoTributaria()),
        factura.fechaEmision(),
        factura.totales().importeTotal(),
        factura.estado().name()
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

  private List<Factura> fetchFacturas(Long empresaId, LocalDate desde, LocalDate hasta, int pageSize) {
    List<Factura> results = new ArrayList<>();
    int page = 0;
    Page<Factura> pageResult;
    do {
      pageResult = facturaRepository.findByEmpresaIdAndFechaEmisionBetween(
          empresaId,
          desde,
          hasta,
          PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "fechaEmision").and(Sort.by("id")))
      );
      results.addAll(pageResult.getContent());
      page++;
    } while (pageResult.hasNext());
    return results;
  }

  private long diasEntre(LocalDate desde, LocalDate hasta) {
    return ChronoUnit.DAYS.between(desde, hasta);
  }

  private static final class ProductoResumen {
    private final Long productoId;
    private final String descripcion;
    private BigDecimal cantidad = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    private ProductoResumen(Long productoId, String descripcion) {
      this.productoId = productoId;
      this.descripcion = descripcion;
    }

    private BigDecimal getTotal() {
      return total;
    }
  }

  private static final class StockResumen {
    private final Long productoId;
    private BigDecimal stockActual = BigDecimal.ZERO;
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    private StockResumen(Long productoId) {
      this.productoId = productoId;
    }

    private BigDecimal getStockActual() {
      return stockActual;
    }
  }
}
