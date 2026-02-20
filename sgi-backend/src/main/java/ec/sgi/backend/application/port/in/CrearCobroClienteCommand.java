package ec.sgi.backend.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record CrearCobroClienteCommand(
    Long empresaId,
    Long clienteId,
    LocalDate fecha,
    String formaPago,
    String referencia,
    BigDecimal montoTotal,
    String observacion,
    List<CobroClienteDetalleCommand> detalles
) {
  public CrearCobroClienteCommand {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(clienteId, "clienteId");
    Objects.requireNonNull(fecha, "fecha");
    Objects.requireNonNull(formaPago, "formaPago");
    Objects.requireNonNull(montoTotal, "montoTotal");
    detalles = List.copyOf(detalles);
  }
}
