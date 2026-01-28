package ec.sgi.backend.application.dto;

import jakarta.validation.constraints.NotBlank;

public record EmpresaCreateRequest(
    @NotBlank String ambiente,
    @NotBlank String tipoEmision,
    @NotBlank String razonSocial,
    @NotBlank String nombreComercial,
    @NotBlank String ruc,
    @NotBlank String dirMatriz,
    @NotBlank String estab,
    @NotBlank String ptoEmi,
    @NotBlank String secuencial,
    Boolean obligadoContabilidad,
    Boolean regimenRimpe
) {
}
