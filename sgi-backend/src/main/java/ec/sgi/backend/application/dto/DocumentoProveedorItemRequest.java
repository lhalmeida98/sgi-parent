package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DocumentoProveedorItemRequest(
    @NotNull Long bodegaId,
    Long productoId,
    Long categoriaId,
    Long impuestoId,
    String codigoPrincipal,
    String descripcion,
    BigDecimal precioVenta,
    @NotNull BigDecimal cantidad,
    @NotNull BigDecimal costoUnitario,
    @NotNull BigDecimal subtotal
) {
}
