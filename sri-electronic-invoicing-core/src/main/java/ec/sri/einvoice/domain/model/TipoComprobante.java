package ec.sri.einvoice.domain.model;

public enum TipoComprobante {
  FACTURA("01"),
  LIQUIDACION_COMPRA_SERVICIOS("03"),
  NOTA_CREDITO("04"),
  NOTA_DEBITO("05"),
  GUIA_REMISION("06"),
  RETENCION("07");

  private final String codigo;

  TipoComprobante(String codigo) {
    this.codigo = codigo;
  }

  public String codigo() {
    return codigo;
  }
}
