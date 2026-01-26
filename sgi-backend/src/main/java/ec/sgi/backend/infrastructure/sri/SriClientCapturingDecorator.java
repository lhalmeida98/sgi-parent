package ec.sgi.backend.infrastructure.sri;

import ec.sri.einvoice.application.port.out.SriClient;
import ec.sri.einvoice.application.port.out.SriResponse;
import ec.sri.einvoice.application.port.out.SriResponseStatus;
import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.infrastructure.sri.SriSoapClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@ConditionalOnBean(SriSoapClient.class)
public class SriClientCapturingDecorator implements SriClient {
  private final SriSoapClient delegate;
  private final SriResponseContext responseContext;

  public SriClientCapturingDecorator(
      SriSoapClient delegate,
      SriResponseContext responseContext
  ) {
    this.delegate = delegate;
    this.responseContext = responseContext;
  }

  @Override
  public SriResponse enviar(Comprobante comprobante, String xmlFirmado) {
    responseContext.clear();
    try {
      SriResponse response = delegate.enviar(comprobante, xmlFirmado);
      SriResponse normalized = normalize(response);
      responseContext.set(normalized);
      return normalized;
    } catch (RuntimeException ex) {
      responseContext.clear();
      throw ex;
    }
  }

  private SriResponse normalize(SriResponse response) {
    if (response == null) {
      return null;
    }
    if (response.status() == SriResponseStatus.NO_AUTORIZADO
        && isEnProcesamiento(response.mensaje())) {
      return new SriResponse(SriResponseStatus.EN_PROCESO, null, response.mensaje());
    }
    return response;
  }

  private boolean isEnProcesamiento(String mensaje) {
    if (mensaje == null || mensaje.isBlank()) {
      return false;
    }
    return mensaje.toUpperCase().contains("CLAVE DE ACCESO EN PROCESAMIENTO");
  }
}
