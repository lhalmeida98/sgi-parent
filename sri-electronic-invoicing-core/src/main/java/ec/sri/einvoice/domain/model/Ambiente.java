package ec.sri.einvoice.domain.model;

public enum Ambiente {
  PRUEBAS("1"),
  PRODUCCION("2");

  private final String codigo;

  Ambiente(String codigo) {
    this.codigo = codigo;
  }

  public String codigo() {
    return codigo;
  }
}
