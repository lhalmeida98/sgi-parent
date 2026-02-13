package ec.sgi.backend.application.port.in;

import java.math.BigDecimal;

public record PagoProveedorDetalleCommand(
    Long cuentaPorPagarId,
    BigDecimal montoAplicado
) {
}
