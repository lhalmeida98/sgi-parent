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
import ec.sgi.backend.domain.model.FacturaEstado;
import ec.sgi.backend.domain.model.Inventario;
import ec.sgi.backend.domain.model.Producto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService implements ObtenerDashboardResumenUseCase {
  private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
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

  @Value("${app.logging.dashboard-timing.enabled:true}")
  private boolean timingEnabled;

  @Value("${app.logging.dashboard-timing.min-ms:200}")
  private long timingMinMs;

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

    BigDecimal ventasMes = timed("ventasMes", () -> calcularVentas(empresaId, inicioMes, hoy));
    BigDecimal ventasMesAnterior = timed("ventasMesAnterior",
        () -> calcularVentas(empresaId, inicioMesAnterior, finMesAnterior));
    BigDecimal variacion = timed("variacionPct", () -> calcularVariacionPct(ventasMes, ventasMesAnterior));

    List<String> estadosCxcExcluidos = List.of(ESTADO_COBRADA, ESTADO_ANULADA);
    BigDecimal saldoCxc = timed("cxc.saldo",
        () -> cuentaPorCobrarRepository.sumSaldoPendienteByEmpresaId(empresaId, estadosCxcExcluidos));
    long pendientesHoy = timed("cxc.pendientesHoy",
        () -> cuentaPorCobrarRepository.countPendientesByEmpresaIdAndFechaVencimiento(
            empresaId, hoy, estadosCxcExcluidos));

    long stockCritico = timed("stockCritico", () -> calcularStockCritico(empresaId));

    List<String> estadosCxpExcluidos = List.of(ESTADO_PAGADO);
    BigDecimal saldoCxp = timed("cxp.saldo",
        () -> cuentaPorPagarRepository.sumSaldoPendienteByEmpresaId(empresaId, estadosCxpExcluidos));
    LocalDate finSemana = hoy.plusDays(7);
    long vencenSemana = timed("cxp.vencenSemana",
        () -> cuentaPorPagarRepository.countVencenEntreFechasByEmpresaId(
            empresaId, hoy, finSemana, estadosCxpExcluidos));

    List<DashboardFlujoCajaResult> flujoCaja = timed("flujoCaja", () -> buildFlujoCaja30Dias(empresaId, hoy));
    List<DashboardFacturaItemResult> ultimasFacturas = timed("ultimasFacturas",
        () -> buildUltimasFacturas(empresaId, hoy));
    List<DashboardProductoVendidoResult> productosMasVendidos = timed("productosMasVendidos",
        () -> buildProductosMasVendidos(empresaId, hoy));
    List<DashboardProductoStockBajoResult> productosMenosStock = timed("productosMenosStock",
        () -> buildProductosMenosStock(empresaId));

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
    return facturaRepository.sumImporteTotalByEmpresaIdAndFechaEmisionBetweenAndEstado(
        empresaId, desde, hasta, FacturaEstado.AUTORIZADA);
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
    return inventarioRepository.countStockCriticoByEmpresaId(empresaId);
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

    List<CobroClienteRepository.FechaTotal> cobros = timed(
        "cobros.sum",
        () -> cobroClienteRepository.sumMontosPorFecha(empresaId, inicio, hoy)
    );
    for (CobroClienteRepository.FechaTotal cobro : cobros) {
      LocalDate fecha = cobro.fecha();
      if (fecha == null || !ingresos.containsKey(fecha)) {
        continue;
      }
      ingresos.put(fecha, ingresos.get(fecha).add(cobro.total()));
    }

    List<PagoProveedorRepository.FechaTotal> pagos = timed(
        "pagos.sum",
        () -> pagoProveedorRepository.sumMontosPorFecha(empresaId, inicio, hoy)
    );
    for (PagoProveedorRepository.FechaTotal pago : pagos) {
      LocalDate fecha = pago.fecha();
      if (fecha == null || !egresos.containsKey(fecha)) {
        continue;
      }
      egresos.put(fecha, egresos.get(fecha).add(pago.total()));
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
    return facturaRepository.findUltimasFacturasResumen(empresaId, desde, hoy, 5);
  }

  private List<DashboardProductoVendidoResult> buildProductosMasVendidos(Long empresaId, LocalDate hoy) {
    LocalDate desde = hoy.minusDays(30);
    return facturaRepository.findProductosMasVendidos(empresaId, desde, hoy, FacturaEstado.AUTORIZADA, 5);
  }

  private List<DashboardProductoStockBajoResult> buildProductosMenosStock(Long empresaId) {
    List<Inventario> inventarios = timed("inventario.fetch",
        () -> inventarioRepository.findByEmpresaId(empresaId));
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
    for (Producto producto : timed("productos.fetch", () -> productoRepository.findByEmpresaId(empresaId))) {
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

  private <T> T timed(String label, Supplier<T> supplier) {
    if (!timingEnabled) {
      return supplier.get();
    }
    long startNs = System.nanoTime();
    try {
      return supplier.get();
    } finally {
      long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
      if (durationMs >= timingMinMs) {
        log.info("dashboard {} ({} ms)", label, durationMs);
      }
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
