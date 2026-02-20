package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CobroClienteResult(
    Long id,
    Long clienteId,
    LocalDate fecha,
    String formaPago,
    String referencia,
    BigDecimal montoTotal,
    String observacion
) {
}
