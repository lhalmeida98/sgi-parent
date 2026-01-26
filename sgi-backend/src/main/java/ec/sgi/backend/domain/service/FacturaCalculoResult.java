package ec.sgi.backend.domain.service;

import ec.sgi.backend.domain.model.FacturaImpuestoTotal;
import ec.sgi.backend.domain.model.FacturaItem;
import ec.sgi.backend.domain.model.FacturaTotales;
import java.util.List;
import java.util.Objects;

public record FacturaCalculoResult(
    List<FacturaItem> items,
    FacturaTotales totales,
    List<FacturaImpuestoTotal> impuestosTotales
) {
  public FacturaCalculoResult {
    items = List.copyOf(items);
    Objects.requireNonNull(totales, "totales");
    impuestosTotales = List.copyOf(impuestosTotales);
  }
}
