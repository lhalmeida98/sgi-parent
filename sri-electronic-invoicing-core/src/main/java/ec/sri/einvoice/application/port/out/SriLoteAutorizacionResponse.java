package ec.sri.einvoice.application.port.out;

import java.util.List;

public record SriLoteAutorizacionResponse(
    String claveAccesoLote,
    List<SriLoteAutorizacionItem> autorizaciones,
    String mensaje
) {
  public static SriLoteAutorizacionResponse error(String mensaje) {
    return new SriLoteAutorizacionResponse(null, List.of(), mensaje);
  }
}
