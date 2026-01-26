package ec.sgi.backend.application.dto;

public record SriInfoTributariaDto(
    String ambiente,
    String tipoEmision,
    String razonSocial,
    String nombreComercial,
    String ruc,
    String dirMatriz,
    String estab,
    String ptoEmi,
    String secuencial,
    String firmaElectronica,
    String claveFirma
) {
}
