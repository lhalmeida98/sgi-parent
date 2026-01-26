package ec.sgi.backend.application.port.in;

import java.util.Objects;

public record SubirFirmaElectronicaCommand(
    Long empresaId,
    String nombreArchivo,
    String tipoContenido,
    byte[] contenido,
    String clave
) {
  public SubirFirmaElectronicaCommand {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(nombreArchivo, "nombreArchivo");
    Objects.requireNonNull(contenido, "contenido");
    Objects.requireNonNull(clave, "clave");
  }
}
