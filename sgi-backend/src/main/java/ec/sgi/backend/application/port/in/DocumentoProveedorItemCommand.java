package ec.sgi.backend.application.port.in;

import java.math.BigDecimal;

public record DocumentoProveedorItemCommand(
    Long bodegaId,
    Long productoId,
    Long categoriaId,
    Long impuestoId,
    String codigoPrincipal,
    String descripcion,
    java.math.BigDecimal precioVenta,
    BigDecimal cantidad,
    BigDecimal costoUnitario,
    BigDecimal subtotal
) {
}
