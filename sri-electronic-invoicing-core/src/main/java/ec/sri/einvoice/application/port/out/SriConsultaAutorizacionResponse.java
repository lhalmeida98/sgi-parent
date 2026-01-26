package ec.sri.einvoice.application.port.out;

public record SriConsultaAutorizacionResponse(
    SriConsultaAutorizacionStatus status,
    String estadoConsulta,
    String estadoAutorizacion,
    String claveAcceso,
    String rucEmisor,
    String tipoComprobante,
    String fechaAutorizacion,
    String mensaje
) {
  public static SriConsultaAutorizacionResponse error(String mensaje) {
    return new SriConsultaAutorizacionResponse(
        SriConsultaAutorizacionStatus.ERROR,
        null,
        null,
        null,
        null,
        null,
        null,
        mensaje
    );
  }
}
