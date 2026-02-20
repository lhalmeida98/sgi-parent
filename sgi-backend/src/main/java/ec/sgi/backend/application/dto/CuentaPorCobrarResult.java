package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CuentaPorCobrarResult(
    Long id,
    Long clienteId,
    Long documentoClienteId,
    String numeroFactura,
    BigDecimal montoOriginal,
    BigDecimal montoCobrado,
    BigDecimal saldo,
    String estado,
    LocalDate fechaVencimiento,
    Integer creditoDias,
    String creditoBucket,
    Integer diasParaVencer,
    boolean vencida,
    String bucketVencimiento
) {
}
