package ec.sri.einvoice.infrastructure.sri;

import ec.sri.einvoice.application.port.out.SriClient;
import ec.sri.einvoice.application.port.out.SriConsultaAutorizacionResponse;
import ec.sri.einvoice.application.port.out.SriConsultaAutorizacionStatus;
import ec.sri.einvoice.application.port.out.SriConsultaClient;
import ec.sri.einvoice.application.port.out.SriConsultaFacturaResponse;
import ec.sri.einvoice.application.port.out.SriConsultaFacturaStatus;
import ec.sri.einvoice.application.port.out.SriLoteAutorizacionItem;
import ec.sri.einvoice.application.port.out.SriLoteAutorizacionResponse;
import ec.sri.einvoice.application.port.out.SriLoteClient;
import ec.sri.einvoice.application.port.out.SriResponse;
import ec.sri.einvoice.domain.model.Ambiente;
import ec.sri.einvoice.domain.model.ClaveAcceso;
import ec.sri.einvoice.domain.model.Comprobante;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.sri", name = "client", havingValue = "dummy")
public class DummySriClient implements SriClient, SriConsultaClient, SriLoteClient {
  @Override
  public SriResponse enviar(Comprobante comprobante, String xmlFirmado) {
    ClaveAcceso claveAcceso = comprobante.claveAcceso();
    if (claveAcceso == null) {
      return SriResponse.error("Clave de acceso faltante");
    }
    char ultimo = claveAcceso.value().charAt(claveAcceso.value().length() - 1);
    int numero = Character.getNumericValue(ultimo);
    if (numero % 2 == 0) {
      return SriResponse.enProceso();
    }
    return SriResponse.noAutorizado("Simulacion SRI: rechazo controlado");
  }

  @Override
  public SriConsultaAutorizacionResponse consultarEstadoAutorizacion(Ambiente ambiente, String claveAcceso) {
    if (isBlank(claveAcceso)) {
      return SriConsultaAutorizacionResponse.error("Clave de acceso requerida");
    }
    boolean autorizado = esPar(claveAcceso);
    String estado = autorizado ? "AUTORIZADO" : "NO AUTORIZADO";
    return new SriConsultaAutorizacionResponse(
        autorizado ? SriConsultaAutorizacionStatus.AUTORIZADO : SriConsultaAutorizacionStatus.NO_AUTORIZADO,
        null,
        estado,
        claveAcceso,
        null,
        null,
        null,
        "Simulacion SRI"
    );
  }

  @Override
  public SriConsultaFacturaResponse consultarEstadoFacturaComercial(Ambiente ambiente, String claveAcceso) {
    if (isBlank(claveAcceso)) {
      return SriConsultaFacturaResponse.error("Clave de acceso requerida");
    }
    boolean confirmado = esPar(claveAcceso);
    String estado = confirmado ? "SI" : "RECHAZADA";
    return new SriConsultaFacturaResponse(
        confirmado ? SriConsultaFacturaStatus.SI : SriConsultaFacturaStatus.RECHAZADA,
        null,
        estado,
        claveAcceso,
        "Simulacion SRI"
    );
  }

  @Override
  public SriResponse enviarLote(Ambiente ambiente, String claveAccesoLote, String rucEmisor, List<String> xmlsFirmados) {
    if (isBlank(claveAccesoLote)) {
      return SriResponse.error("Clave de acceso de lote requerida");
    }
    return SriResponse.recibido();
  }

  @Override
  public SriLoteAutorizacionResponse consultarAutorizacionLote(Ambiente ambiente, String claveAccesoLote) {
    if (isBlank(claveAccesoLote)) {
      return SriLoteAutorizacionResponse.error("Clave de acceso de lote requerida");
    }
    boolean autorizado = esPar(claveAccesoLote);
    SriLoteAutorizacionItem item = new SriLoteAutorizacionItem(
        claveAccesoLote,
        autorizado ? "AUTORIZADO" : "NO AUTORIZADO",
        autorizado ? "AUT-" + claveAccesoLote : null,
        "Simulacion SRI"
    );
    return new SriLoteAutorizacionResponse(claveAccesoLote, List.of(item), null);
  }

  private boolean esPar(String claveAcceso) {
    char ultimo = claveAcceso.charAt(claveAcceso.length() - 1);
    int numero = Character.getNumericValue(ultimo);
    return numero % 2 == 0;
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
