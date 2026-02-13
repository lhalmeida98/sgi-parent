package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CuentaPorPagarResult(
    Long id,
    Long proveedorId,
    Long documentoProveedorId,
    String documentoNumero,
    String documentoTipo,
    BigDecimal montoOriginal,
    BigDecimal montoPagado,
    BigDecimal saldo,
    String estado,
    LocalDate fechaVencimiento
) {
}
