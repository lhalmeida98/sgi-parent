package ec.sgi.backend.domain.model;

import java.util.Objects;

public record Empresa(
    Long id,
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
  public Empresa {
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

  public Empresa withSecuencial(String nuevoSecuencial) {
    return new Empresa(
        id,
        ambiente,
        tipoEmision,
        razonSocial,
        nombreComercial,
        ruc,
        dirMatriz,
        estab,
        ptoEmi,
        nuevoSecuencial
    );
  }
}
