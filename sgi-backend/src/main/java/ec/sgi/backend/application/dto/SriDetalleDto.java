package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record SriDetalleDto(
    String codigoPrincipal,
    String descripcion,
    BigDecimal cantidad,
    BigDecimal precioUnitario,
    BigDecimal descuento,
    BigDecimal precioTotalSinImpuesto,
    List<SriImpuestoDto> impuestos
) {
}
