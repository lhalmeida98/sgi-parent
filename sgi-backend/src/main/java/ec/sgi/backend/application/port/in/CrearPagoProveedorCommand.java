package ec.sgi.backend.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CrearPagoProveedorCommand(
    Long empresaId,
    Long proveedorId,
    LocalDate fechaPago,
    String formaPago,
    String referencia,
    BigDecimal montoTotal,
    String observacion,
    List<PagoProveedorDetalleCommand> detalles
) {
}
