package ec.sgi.backend.domain.service;

import ec.sgi.backend.domain.model.FacturaImpuesto;
import ec.sgi.backend.domain.model.FacturaImpuestoTotal;
import ec.sgi.backend.domain.model.FacturaItem;
import ec.sgi.backend.domain.model.FacturaTotales;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacturaTotalsCalculator {
  private static final int SCALE = 2;

  public FacturaCalculoResult calcular(List<ItemCalculo> items) {
    List<FacturaItem> detalles = new ArrayList<>();
    BigDecimal totalSinImpuestos = BigDecimal.ZERO;
    BigDecimal totalDescuento = BigDecimal.ZERO;
    BigDecimal totalImpuestos = BigDecimal.ZERO;

    Map<String, FacturaImpuestoTotal> impuestosTotales = new HashMap<>();

    for (ItemCalculo item : items) {
      BigDecimal subtotal = scale(item.precioUnitario().multiply(item.cantidad()));
      BigDecimal descuento = scale(item.descuento());
      BigDecimal baseImponible = scale(subtotal.subtract(descuento));
      BigDecimal impuestoValor = scale(baseImponible.multiply(item.impuestoTarifa())
          .divide(BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP));

      FacturaImpuesto impuesto = new FacturaImpuesto(
          item.impuestoCodigo(),
          item.impuestoCodigoPorcentaje(),
          scale(item.impuestoTarifa()),
          baseImponible,
          impuestoValor
      );

      FacturaItem detalle = new FacturaItem(
          item.productoId(),
          item.codigoPrincipal(),
          item.descripcion(),
          scale(item.cantidad()),
          scale(item.precioUnitario()),
          descuento,
          baseImponible,
          List.of(impuesto)
      );
      detalles.add(detalle);

      totalSinImpuestos = totalSinImpuestos.add(baseImponible);
      totalDescuento = totalDescuento.add(descuento);
      totalImpuestos = totalImpuestos.add(impuestoValor);

      String key = item.impuestoCodigo() + "|" + item.impuestoCodigoPorcentaje();
      FacturaImpuestoTotal acumulado = impuestosTotales.get(key);
      if (acumulado == null) {
        impuestosTotales.put(key, new FacturaImpuestoTotal(
            item.impuestoCodigo(),
            item.impuestoCodigoPorcentaje(),
            baseImponible,
            impuestoValor
        ));
      } else {
        impuestosTotales.put(key, new FacturaImpuestoTotal(
            acumulado.codigo(),
            acumulado.codigoPorcentaje(),
            scale(acumulado.baseImponible().add(baseImponible)),
            scale(acumulado.valor().add(impuestoValor))
        ));
      }
    }

    totalSinImpuestos = scale(totalSinImpuestos);
    totalDescuento = scale(totalDescuento);
    totalImpuestos = scale(totalImpuestos);
    BigDecimal importeTotal = scale(totalSinImpuestos.add(totalImpuestos));

    FacturaTotales totales = new FacturaTotales(
        totalSinImpuestos,
        totalDescuento,
        totalImpuestos,
        importeTotal
    );

    return new FacturaCalculoResult(detalles, totales, new ArrayList<>(impuestosTotales.values()));
  }

  private BigDecimal scale(BigDecimal value) {
    return value.setScale(SCALE, RoundingMode.HALF_UP);
  }
}
