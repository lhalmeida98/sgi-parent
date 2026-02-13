package ec.sgi.backend.application.port.in;

public record CrearDocumentoProveedorAutorizacionCommand(
    Long empresaId,
    Long proveedorId,
    Long bodegaId,
    String numeroAutorizacion
) {
}
