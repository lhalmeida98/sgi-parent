package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record SubirLogoEmpresaCommand(
    Long empresaId,
    String nombreArchivo,
    String tipoContenido,
    byte[] contenido
) {
  public SubirLogoEmpresaCommand {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(nombreArchivo, "nombreArchivo");
    Objects.requireNonNull(contenido, "contenido");
  }
}
