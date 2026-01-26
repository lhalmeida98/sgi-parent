package ec.sgi.backend.application.port.in;

import java.math.BigDecimal;
import java.util.Objects;

public record CrearImpuestoCommand(
    Long empresaId,
    String codigo,
    String codigoPorcentaje,
    BigDecimal tarifa,
    String descripcion,
    Boolean activo
) {
  public CrearImpuestoCommand {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(codigo, "codigo");
    Objects.requireNonNull(codigoPorcentaje, "codigoPorcentaje");
    Objects.requireNonNull(tarifa, "tarifa");
  }
}
