package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record ProductoResult(
    Long id,
    String codigo,
    String descripcion,
    BigDecimal precioUnitario,
    Long categoriaId,
    Long impuestoId,
    Long proveedorId,
    String proveedorRuc,
    String proveedorNombre,
    boolean vendible,
    String codigoBarras
) {
}
