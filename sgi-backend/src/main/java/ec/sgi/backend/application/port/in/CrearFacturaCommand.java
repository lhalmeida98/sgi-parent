package ec.sgi.backend.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record CrearFacturaCommand(
    Long empresaId,
    Long clienteId,
    Long preordenId,
    String dirEstablecimiento,
    LocalDate fechaEmision,
    String moneda,
    String codigoNumerico,
    String observaciones,
    List<ItemFacturaCommand> items,
    List<PagoFacturaCommand> pagos
) {
  public CrearFacturaCommand {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(clienteId, "clienteId");
    Objects.requireNonNull(fechaEmision, "fechaEmision");
    Objects.requireNonNull(moneda, "moneda");
    Objects.requireNonNull(codigoNumerico, "codigoNumerico");
    items = List.copyOf(items);
    pagos = List.copyOf(pagos);
  }
}
