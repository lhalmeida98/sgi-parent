package ec.sgi.backend.application.port.in;

public record CrearDocumentoProveedorXmlCommand(
    Long empresaId,
    Long proveedorId,
    Long bodegaId,
    String xml
) {
}
