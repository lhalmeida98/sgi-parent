package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DocumentoProveedorPreviewResult(
    String tipoDocumento,
    String numeroDocumento,
    String numeroAutorizacion,
    LocalDate fechaEmision,
    BigDecimal subtotal,
    BigDecimal impuestos,
    BigDecimal total,
    String moneda,
    String identificacionEmisor,
    String razonSocialEmisor,
    List<DocumentoProveedorPreviewItemResult> items
) {
}
