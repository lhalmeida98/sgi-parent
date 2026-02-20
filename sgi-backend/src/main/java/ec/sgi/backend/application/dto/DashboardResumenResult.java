package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResumenResult(
    BigDecimal ventasMes,
    BigDecimal ventasMesAnterior,
    BigDecimal ventasVariacionPct,
    BigDecimal cuentasPorCobrarTotal,
    long cuentasPorCobrarPendientesHoy,
    long stockCritico,
    BigDecimal proveedoresPorPagarTotal,
    long proveedoresPorPagarVencenSemana,
    List<DashboardFlujoCajaResult> flujoCaja30Dias,
    List<DashboardFacturaItemResult> ultimasFacturas,
    List<DashboardProductoVendidoResult> productosMasVendidos,
    List<DashboardProductoStockBajoResult> productosMenosStock
) {
}
