package ec.sgi.backend.domain.model;

import java.util.Objects;

public record InfoTributariaData(
    String ambiente,
    String tipoEmision,
    String razonSocial,
    String nombreComercial,
    String ruc,
    String dirMatriz,
    String estab,
    String ptoEmi,
    String secuencial
) {
  public InfoTributariaData {
    Objects.requireNonNull(ambiente, "ambiente");
    Objects.requireNonNull(tipoEmision, "tipoEmision");
    Objects.requireNonNull(razonSocial, "razonSocial");
    Objects.requireNonNull(nombreComercial, "nombreComercial");
    Objects.requireNonNull(ruc, "ruc");
    Objects.requireNonNull(dirMatriz, "dirMatriz");
    Objects.requireNonNull(estab, "estab");
    Objects.requireNonNull(ptoEmi, "ptoEmi");
    Objects.requireNonNull(secuencial, "secuencial");
  }
}
