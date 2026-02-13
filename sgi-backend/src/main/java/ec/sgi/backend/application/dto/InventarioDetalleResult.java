package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record InventarioDetalleResult(
    Long productoId,
    String productoNombre,
    Long bodegaId,
    String bodegaNombre,
    BigDecimal stockActual,
    BigDecimal stockReservado,
    BigDecimal stockMinimo,
    BigDecimal stockMaximo,
    String ubicacion,
    BigDecimal costoPromedio,
    BigDecimal precioVenta,
    BigDecimal margenPorcentaje,
    BigDecimal stockGlobal,
    BigDecimal stockReservadoGlobal,
    BigDecimal costoPromedioGlobal,
    BigDecimal margenPorcentajeGlobal
) {
}
