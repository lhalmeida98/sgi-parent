package ec.sgi.backend.application.port.in;

import ec.sgi.backend.domain.model.RegimenTributario;
import java.util.Objects;

public record ActualizarEmpresaCommand(
    String ambiente,
    String tipoEmision,
    String razonSocial,
    String nombreComercial,
    String dirMatriz,
    String estab,
    String ptoEmi,
    String secuencial,
    boolean obligadoContabilidad,
    RegimenTributario regimenTributario,
    boolean contribuyenteEspecial,
    String numeroContribuyenteEspecial,
    boolean agenteRetencion,
    Integer creditoDiasDefault
) {
  public ActualizarEmpresaCommand {
    Objects.requireNonNull(ambiente, "ambiente");
    Objects.requireNonNull(tipoEmision, "tipoEmision");
    Objects.requireNonNull(razonSocial, "razonSocial");
    Objects.requireNonNull(nombreComercial, "nombreComercial");
    Objects.requireNonNull(dirMatriz, "dirMatriz");
    Objects.requireNonNull(estab, "estab");
    Objects.requireNonNull(ptoEmi, "ptoEmi");
    Objects.requireNonNull(secuencial, "secuencial");
  }
}
