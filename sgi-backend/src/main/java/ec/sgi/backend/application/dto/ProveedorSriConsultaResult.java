package ec.sgi.backend.application.dto;

public record ProveedorSriConsultaResult(
    boolean encontrado,
    String mensaje,
    SriContribuyenteInfo data
) {
}
