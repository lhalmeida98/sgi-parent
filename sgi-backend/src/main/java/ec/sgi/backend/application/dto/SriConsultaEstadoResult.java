package ec.sgi.backend.application.dto;

public record SriConsultaEstadoResult(
    String estadoConsulta,
    String estadoAutorizacion,
    String mensaje,
    String claveAcceso,
    String xmlAutorizado
) {
}
