package ec.sri.einvoice.application.port.out;

public record SriResponse(
    SriResponseStatus status,
    String numeroAutorizacion,
    String mensaje
) {
  public static SriResponse generado() {
    return new SriResponse(SriResponseStatus.GENERADO, null, null);
  }

  public static SriResponse enviadoSri() {
    return new SriResponse(SriResponseStatus.ENVIADO_SRI, null, null);
  }

  public static SriResponse enProceso() {
    return new SriResponse(SriResponseStatus.EN_PROCESO, null, null);
  }

  public static SriResponse autorizado(String numero) {
    return new SriResponse(SriResponseStatus.AUTORIZADO, numero, null);
  }

  public static SriResponse noAutorizado(String mensaje) {
    return new SriResponse(SriResponseStatus.NO_AUTORIZADO, null, mensaje);
  }

  public static SriResponse error(String mensaje) {
    return new SriResponse(SriResponseStatus.ERROR, null, mensaje);
  }

  public static SriResponse recibido() {
    return enProceso();
  }

  public static SriResponse rechazado(String mensaje) {
    return noAutorizado(mensaje);
  }
}
