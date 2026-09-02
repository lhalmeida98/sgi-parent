package ec.sgi.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SriInfoFacturaDto(
    LocalDate fechaEmision,
    String dirEstablecimiento,
    String contribuyenteEspecial,
    String obligadoContabilidad,
    String tipoIdentificacionComprador,
    String razonSocialComprador,
    String identificacionComprador,
    String direccionComprador,
    BigDecimal totalSinImpuestos,
    BigDecimal totalDescuento,
    BigDecimal propina,
    BigDecimal importeTotal,
    String moneda,
    List<SriTotalImpuestoDto> totalConImpuestos
) {
}
