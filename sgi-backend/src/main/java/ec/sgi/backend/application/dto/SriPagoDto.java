package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record SriPagoDto(
    String formaPago,
    BigDecimal total,
    Integer plazo,
    String unidadTiempo
) {
}
