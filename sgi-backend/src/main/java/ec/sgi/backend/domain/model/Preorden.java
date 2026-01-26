package ec.sgi.backend.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record Preorden(
    Long id,
    Long empresaId,
    Long clienteId,
    LocalDateTime fechaCreacion,
    String dirEstablecimiento,
    String moneda,
    FacturaTotales totales,
    String estado,
    String observaciones,
    boolean reservaInventario,
    List<PreordenItem> items
) {
  public Preorden {
    Objects.requireNonNull(clienteId, "clienteId");
    Objects.requireNonNull(fechaCreacion, "fechaCreacion");
    Objects.requireNonNull(dirEstablecimiento, "dirEstablecimiento");
    Objects.requireNonNull(moneda, "moneda");
    Objects.requireNonNull(totales, "totales");
    Objects.requireNonNull(estado, "estado");
    items = List.copyOf(items);
  }
}
