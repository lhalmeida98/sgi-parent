package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoProveedorResult(
    Long id,
    Long proveedorId,
    LocalDate fechaPago,
    String formaPago,
    String referencia,
    BigDecimal montoTotal,
    String observacion
) {
}
