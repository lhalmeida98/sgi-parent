package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record CobroClienteDetalle(
    Long id,
    Long cuentaPorCobrarId,
    BigDecimal montoAplicado
) {
  public CobroClienteDetalle {
    Objects.requireNonNull(cuentaPorCobrarId, "cuentaPorCobrarId");
    Objects.requireNonNull(montoAplicado, "montoAplicado");
  }
}
