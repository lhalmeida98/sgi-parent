package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record FacturaPagoDto(
    String formaPago,
    BigDecimal monto
) {
}
