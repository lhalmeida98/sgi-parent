package ec.sri.einvoice.domain.model;

import java.util.Objects;

public record CampoAdicional(String nombre, String valor) {
  public CampoAdicional {
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(valor, "valor");
  }
}
