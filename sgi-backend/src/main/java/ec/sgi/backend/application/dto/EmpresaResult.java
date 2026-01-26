package ec.sgi.backend.application.dto;


public record EmpresaResult(
    Long id,
    String ambiente,
    String tipoEmision,
    String razonSocial,
    String nombreComercial,
    String ruc,
    String dirMatriz,
    String estab,
    String ptoEmi,
    String secuencial
) {
}
