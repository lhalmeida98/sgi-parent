package ec.sgi.backend.application.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record CrearPreordenCommand(
    Long empresaId,
    Long clienteId,
    String dirEstablecimiento,
    String moneda,
    String observaciones,
    Boolean reservaInventario,
    List<ItemPreordenCommand> items
) {
  public CrearPreordenCommand {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(clienteId, "clienteId");
    Objects.requireNonNull(dirEstablecimiento, "dirEstablecimiento");
    Objects.requireNonNull(moneda, "moneda");
    items = List.copyOf(items);
  }

  public record ItemPreordenCommand(
      Long productoId,
      BigDecimal cantidad,
      BigDecimal descuento
  ) {
    public ItemPreordenCommand {
      Objects.requireNonNull(productoId, "productoId");
      Objects.requireNonNull(cantidad, "cantidad");
      Objects.requireNonNull(descuento, "descuento");
    }
  }
}
