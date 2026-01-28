package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record CrearEmpresaCommand(
    String ambiente,
    String tipoEmision,
    String razonSocial,
    String nombreComercial,
    String ruc,
    String dirMatriz,
    String estab,
    String ptoEmi,
    String secuencial,
    boolean obligadoContabilidad,
    boolean regimenRimpe
) {
  public CrearEmpresaCommand {
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
