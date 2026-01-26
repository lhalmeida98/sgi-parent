package ec.sri.einvoice.domain.service;

import ec.sri.einvoice.domain.model.ClaveAcceso;
import ec.sri.einvoice.domain.model.InfoTributaria;
import ec.sri.einvoice.domain.model.TipoComprobante;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ClaveAccesoGenerator {
  private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("ddMMyyyy");
  private static final int RUC_LENGTH = 13;
  private static final int ESTAB_LENGTH = 3;
  private static final int PUNTO_EMISION_LENGTH = 3;
  private static final int SECUENCIAL_LENGTH = 9;
  private static final int CODIGO_NUMERICO_LENGTH = 8;

  public ClaveAcceso generar(
      InfoTributaria infoTributaria,
      TipoComprobante tipoComprobante,
      String codigoNumerico,
      LocalDate fechaEmision
  ) {
    String fecha = fechaEmision.format(FECHA_FORMATO);
    String codigoDoc = tipoComprobante.codigo();
    String ruc = requireExactDigits(infoTributaria.ruc(), RUC_LENGTH, "ruc");
    String ambiente = infoTributaria.ambiente().codigo();
    String serie = padLeftNumeric(infoTributaria.estab(), ESTAB_LENGTH, "estab")
        + padLeftNumeric(infoTributaria.ptoEmi(), PUNTO_EMISION_LENGTH, "ptoEmi");
    String secuencial = padLeftNumeric(infoTributaria.secuencial(), SECUENCIAL_LENGTH, "secuencial");
    String codigo = padLeftNumeric(codigoNumerico, CODIGO_NUMERICO_LENGTH, "codigoNumerico");
    String tipoEmision = infoTributaria.tipoEmision().codigo();

    String base = fecha + codigoDoc + ruc + ambiente + serie + secuencial + codigo + tipoEmision;
    int digitoVerificador = modulo11(base);
    return ClaveAcceso.of(base + digitoVerificador);
  }

  private String padLeftNumeric(String value, int length, String fieldName) {
    String normalized = requireDigits(value, fieldName);
    if (normalized.length() > length) {
      throw new IllegalArgumentException("Campo " + fieldName + " supera longitud " + length);
    }
    return String.format("%" + length + "s", normalized).replace(' ', '0');
  }

  private String requireExactDigits(String value, int length, String fieldName) {
    String normalized = requireDigits(value, fieldName);
    if (normalized.length() != length) {
      throw new IllegalArgumentException("Campo " + fieldName + " debe tener " + length + " digitos");
    }
    return normalized;
  }

  private String requireDigits(String value, String fieldName) {
    if (value == null) {
      throw new IllegalArgumentException("Campo " + fieldName + " es requerido");
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Campo " + fieldName + " es requerido");
    }
    for (int i = 0; i < trimmed.length(); i++) {
      if (!Character.isDigit(trimmed.charAt(i))) {
        throw new IllegalArgumentException("Campo " + fieldName + " debe ser numerico");
      }
    }
    return trimmed;
  }

  private int modulo11(String value) {
    int suma = 0;
    int factor = 2;
    for (int i = value.length() - 1; i >= 0; i--) {
      int num = Character.getNumericValue(value.charAt(i));
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
