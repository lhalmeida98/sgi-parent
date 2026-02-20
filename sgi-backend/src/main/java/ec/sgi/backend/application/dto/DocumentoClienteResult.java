package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DocumentoClienteResult(
    Long id,
    Long clienteId,
    Long facturaId,
    String claveAcceso,
    String numeroFactura,
    LocalDate fechaEmision,
    LocalDate fechaVencimiento,
    BigDecimal total,
    String estado,
    Integer diasParaVencer,
    boolean vencida
) {
}
