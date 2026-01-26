package ec.sri.einvoice.application.port.out;

public record SriLoteAutorizacionItem(
    String claveAcceso,
    String estado,
    String numeroAutorizacion,
    String mensaje
) {
}
