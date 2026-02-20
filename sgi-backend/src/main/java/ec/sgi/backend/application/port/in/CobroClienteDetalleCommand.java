package ec.sgi.backend.application.port.in;

import java.math.BigDecimal;

public record CobroClienteDetalleCommand(
    Long cuentaPorCobrarId,
    BigDecimal montoAplicado
) {
}
