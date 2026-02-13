package ec.sgi.backend.application.dto;

import java.math.BigDecimal;

public record ProductoResult(
    Long id,
    String codigo,
    String descripcion,
    BigDecimal precioUnitario,
    Long categoriaId,
    Long impuestoId,
    boolean vendible,
    String codigoBarras
) {
}
