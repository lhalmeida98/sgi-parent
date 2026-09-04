package ec.sgi.backend.application.dto;

public record ClienteSriConsultaResult(
    boolean encontrado,
    String mensaje,
    SriContribuyenteInfo data
) {
}
