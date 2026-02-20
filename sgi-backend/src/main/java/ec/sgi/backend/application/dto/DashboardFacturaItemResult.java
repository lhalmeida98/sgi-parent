package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardFacturaItemResult(
    Long id,
    String numeroFactura,
    LocalDate fechaEmision,
    BigDecimal total,
    String estado
) {
}
