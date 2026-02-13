package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record InventarioProductoDisponibleResult(
    Long productoId,
    String codigo,
    String descripcion,
    BigDecimal precioUnitario,
    Long categoriaId,
    Long impuestoId,
    boolean vendible,
    String codigoBarras,
    Long bodegaId,
    String bodegaNombre,
    BigDecimal stockActual,
    BigDecimal stockReservado,
    BigDecimal stockDisponible,
    BigDecimal stockMinimo,
    BigDecimal stockMaximo,
    String ubicacion,
    BigDecimal costoPromedio
) {
}
