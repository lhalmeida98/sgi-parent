package ec.sri.einvoice.domain.model;

public enum TipoEmision {
  NORMAL("1"),
  CONTINGENCIA("2");

  private final String codigo;

  TipoEmision(String codigo) {
    this.codigo = codigo;
  }

  public String codigo() {
    return codigo;
  }
}
