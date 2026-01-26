package ec.sri.einvoice.domain.model;

import java.util.Objects;

public final class ClaveAcceso {
  private final String value;

  private ClaveAcceso(String value) {
    this.value = value;
  }

  public static ClaveAcceso of(String value) {
    Objects.requireNonNull(value, "value");
    if (value.length() != 49) {
      throw new IllegalArgumentException("La clave de acceso debe tener 49 digitos");
    }
    for (int i = 0; i < value.length(); i++) {
      if (!Character.isDigit(value.charAt(i))) {
        throw new IllegalArgumentException("La clave de acceso debe ser numerica");
      }
    }
    String base = value.substring(0, 48);
    int expected = modulo11(base);
    int actual = value.charAt(48) - '0';
    if (expected != actual) {
      throw new IllegalArgumentException("Digito verificador invalido");
    }
    return new ClaveAcceso(value);
  }

  public String value() {
    return value;
  }

  private static int modulo11(String value) {
    int suma = 0;
    int factor = 2;
    for (int i = value.length() - 1; i >= 0; i--) {
      int num = value.charAt(i) - '0';
      suma += num * factor;
      factor = (factor == 7) ? 2 : factor + 1;
    }
    int mod = 11 - (suma % 11);
    if (mod == 11) {
      return 0;
    }
    if (mod == 10) {
      return 1;
    }
    return mod;
  }
}
