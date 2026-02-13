package ec.sgi.backend.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CrearDocumentoProveedorCommand(
    Long empresaId,
    Long proveedorId,
    String tipoDocumento,
    String numeroDocumento,
    String numeroAutorizacion,
    LocalDate fechaEmision,
    LocalDate fechaVencimiento,
    BigDecimal subtotal,
    BigDecimal impuestos,
    BigDecimal total,
    String moneda,
    List<DocumentoProveedorItemCommand> items
) {
}
