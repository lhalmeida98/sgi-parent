package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DocumentoProveedorResult(
    Long id,
    Long proveedorId,
    String tipoDocumento,
    String numeroDocumento,
    String numeroAutorizacion,
    LocalDate fechaEmision,
    LocalDate fechaVencimiento,
    BigDecimal total,
    String moneda,
    String estado
) {
}
