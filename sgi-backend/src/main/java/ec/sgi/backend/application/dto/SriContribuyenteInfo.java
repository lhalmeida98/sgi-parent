package ec.sgi.backend.application.dto;

public record SriContribuyenteInfo(
    String numeroRuc,
    String razonSocial,
    String estadoContribuyenteRuc,
    String actividadEconomicaPrincipal,
    String tipoContribuyente,
    String regimen,
    String categoria,
    String obligadoLlevarContabilidad,
    String direccionCompleta,
    String agenteRetencion,
    String contribuyenteEspecial,
    String contribuyenteFantasma,
    String transaccionesInexistente
) {
}
