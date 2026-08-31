package ec.sgi.backend.domain.model;

import java.util.Locale;

public enum RegimenTributario {
  GENERAL,
  RIMPE_EMPRENDEDOR,
  RIMPE_NEGOCIO_POPULAR;

  public static RegimenTributario from(String value, boolean regimenRimpeFallback) {
    if (value == null || value.isBlank()) {
      return regimenRimpeFallback ? RIMPE_EMPRENDEDOR : GENERAL;
    }
    String normalized = value.trim()
        .toUpperCase(Locale.ROOT)
        .replace(' ', '_')
        .replace('-', '_');
    return switch (normalized) {
      case "GENERAL" -> GENERAL;
      case "RIMPE", "RIMPE_EMPRENDEDOR", "EMPRENDEDOR" -> RIMPE_EMPRENDEDOR;
      case "RIMPE_NEGOCIO_POPULAR", "NEGOCIO_POPULAR" -> RIMPE_NEGOCIO_POPULAR;
      default -> throw new IllegalArgumentException("Regimen tributario no valido: " + value);
    };
  }

  public boolean esRimpe() {
    return this == RIMPE_EMPRENDEDOR || this == RIMPE_NEGOCIO_POPULAR;
  }

  public String leyendaSri() {
    return switch (this) {
      case GENERAL -> null;
      case RIMPE_EMPRENDEDOR -> "CONTRIBUYENTE RÉGIMEN RIMPE";
      case RIMPE_NEGOCIO_POPULAR -> "CONTRIBUYENTE NEGOCIO POPULAR - RÉGIMEN RIMPE";
    };
  }
}
