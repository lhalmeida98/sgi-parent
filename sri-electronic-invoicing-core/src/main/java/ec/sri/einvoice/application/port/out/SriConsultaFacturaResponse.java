package ec.sri.einvoice.application.port.out;

public record SriConsultaFacturaResponse(
    SriConsultaFacturaStatus status,
    String estadoConsulta,
    String estadoConfirmacion,
    String claveAcceso,
    String mensaje
) {
  public static SriConsultaFacturaResponse error(String mensaje) {
    return new SriConsultaFacturaResponse(
        SriConsultaFacturaStatus.ERROR,
        null,
        null,
        null,
        mensaje
    );
  }
}
