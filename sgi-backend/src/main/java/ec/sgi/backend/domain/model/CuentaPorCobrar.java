package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record CuentaPorCobrar(
    Long id,
    Long empresaId,
    Long clienteId,
    Long documentoClienteId,
    BigDecimal montoOriginal,
    BigDecimal montoCobrado,
    BigDecimal saldo,
    String estado,
    LocalDate fechaVencimiento,
    Integer creditoDias,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
  public CuentaPorCobrar {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(clienteId, "clienteId");
    Objects.requireNonNull(documentoClienteId, "documentoClienteId");
    Objects.requireNonNull(montoOriginal, "montoOriginal");
    Objects.requireNonNull(montoCobrado, "montoCobrado");
    Objects.requireNonNull(saldo, "saldo");
    Objects.requireNonNull(estado, "estado");
  }
}
