package ec.sgi.backend.application.port.in;

public interface ObtenerFacturaXmlUseCase {
  String obtenerXml(Long facturaId, Long empresaId);
}
