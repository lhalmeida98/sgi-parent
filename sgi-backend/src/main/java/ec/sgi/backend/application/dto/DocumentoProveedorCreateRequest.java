package ec.sgi.backend.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DocumentoProveedorCreateRequest(
    @NotBlank String tipoDocumento,
    @NotBlank String numeroDocumento,
    String numeroAutorizacion,
    @NotNull LocalDate fechaEmision,
    LocalDate fechaVencimiento,
    @NotNull BigDecimal subtotal,
    @NotNull BigDecimal impuestos,
    @NotNull BigDecimal total,
    @NotBlank String moneda,
    @Valid List<DocumentoProveedorItemRequest> items
) {
}
