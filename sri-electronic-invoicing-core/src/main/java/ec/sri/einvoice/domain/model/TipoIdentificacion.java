package ec.sri.einvoice.domain.model;

public enum TipoIdentificacion {
  RUC("04"),
  CEDULA("05"),
  PASAPORTE("06"),
  CONSUMIDOR_FINAL("07"),
  IDENTIFICACION_EXTERIOR("08");

  private final String codigo;

  TipoIdentificacion(String codigo) {
    this.codigo = codigo;
  }

  public String codigo() {
    return codigo;
  }
}
