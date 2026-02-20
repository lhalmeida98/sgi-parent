package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record DocumentoCliente(
    Long id,
    Long empresaId,
    Long clienteId,
    Long facturaId,
    String claveAcceso,
    String numeroFactura,
    LocalDate fechaEmision,
    LocalDate fechaVencimiento,
    BigDecimal total,
    String estado,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
  public DocumentoCliente {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(clienteId, "clienteId");
    Objects.requireNonNull(numeroFactura, "numeroFactura");
    Objects.requireNonNull(fechaEmision, "fechaEmision");
    Objects.requireNonNull(total, "total");
    Objects.requireNonNull(estado, "estado");
  }
}
