package ec.sgi.backend.application.port.in;

public interface GenerarFacturaPdfUseCase {
  byte[] generar(GenerarFacturaPdfCommand command);
}
