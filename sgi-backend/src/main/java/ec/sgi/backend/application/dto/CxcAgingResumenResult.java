package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record CxcAgingResumenResult(
    long totalCuentas,
    BigDecimal totalSaldo,
    long vencidas,
    BigDecimal saldoVencido,
    long porVencer7,
    BigDecimal saldoPorVencer7,
    long porVencer15,
    BigDecimal saldoPorVencer15,
    long porVencer30,
    BigDecimal saldoPorVencer30,
    long futuras,
    BigDecimal saldoFuturo
) {
}
