package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record CobroCliente(
    Long id,
    Long empresaId,
    Long clienteId,
    LocalDate fecha,
    String formaPago,
    String referencia,
    BigDecimal montoTotal,
    String observacion,
    List<CobroClienteDetalle> detalles,
    LocalDateTime creadoEn
) {
  public CobroCliente {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(clienteId, "clienteId");
    Objects.requireNonNull(fecha, "fecha");
    Objects.requireNonNull(formaPago, "formaPago");
    Objects.requireNonNull(montoTotal, "montoTotal");
    detalles = detalles == null ? List.of() : List.copyOf(detalles);
  }
}
