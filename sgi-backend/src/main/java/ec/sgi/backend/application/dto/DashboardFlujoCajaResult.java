package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardFlujoCajaResult(
    LocalDate fecha,
    BigDecimal ingresos,
    BigDecimal egresos,
    BigDecimal neto
) {
}
