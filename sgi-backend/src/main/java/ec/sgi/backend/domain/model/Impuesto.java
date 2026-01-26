package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Impuesto(
    Long id,
    Long empresaId,
    String codigo,
    String codigoPorcentaje,
    BigDecimal tarifa,
    String descripcion,
    boolean activo
) {
  public Impuesto {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(codigo, "codigo");
    Objects.requireNonNull(codigoPorcentaje, "codigoPorcentaje");
    Objects.requireNonNull(tarifa, "tarifa");
  }
}
