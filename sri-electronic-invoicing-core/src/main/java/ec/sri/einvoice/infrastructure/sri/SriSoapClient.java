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
import ec.sri.einvoice.application.port.out.SriResponseStatus;
import ec.sri.einvoice.domain.model.Ambiente;
import ec.sri.einvoice.domain.model.ClaveAcceso;
import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.infrastructure.config.AppProperties;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
@ConditionalOnProperty(prefix = "app.sri", name = "client", havingValue = "soap", matchIfMissing = true)
public class SriSoapClient implements SriClient, SriConsultaClient, SriLoteClient {
  private final AppProperties properties;
  private final HttpClient httpClient;
  private final SriLoteXmlBuilder loteXmlBuilder;

  public SriSoapClient(AppProperties properties) {
    this.properties = properties;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(properties.getSri().getTimeoutSeconds()))
        .build();
    this.loteXmlBuilder = new SriLoteXmlBuilder();
  }

  @Override
  public SriResponse enviar(Comprobante comprobante, String xmlFirmado) {
    ClaveAcceso claveAcceso = comprobante.claveAcceso();
    if (claveAcceso == null) {
      return SriResponse.error("Clave de acceso faltante");
    }

    AppProperties.Endpoints endpoints = endpointsFor(comprobante.infoTributaria().ambiente());
    if (endpoints == null || isBlank(endpoints.getRecepcionUrl()) || isBlank(endpoints.getAutorizacionUrl())) {
      return SriResponse.error("Endpoints SRI no configurados");
    }

    // Flujo SRI: solo recepcion en esta llamada; la autorizacion se consulta luego.
    return enviarRecepcion(endpoints.getRecepcionUrl(), xmlFirmado);
  }

  @Override
  public SriConsultaAutorizacionResponse consultarEstadoAutorizacion(Ambiente ambiente, String claveAcceso) {
    if (isBlank(claveAcceso)) {
      return SriConsultaAutorizacionResponse.error("Clave de acceso requerida para consulta");
    }
    AppProperties.Endpoints endpoints = endpointsFor(ambiente);
    if (endpoints == null || isBlank(endpoints.getConsultaComprobanteUrl())) {
      return SriConsultaAutorizacionResponse.error("Endpoint de consulta no configurado");
    }
    String envelope = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ec="http://ec.gob.sri.ws.consultas">
          <soapenv:Header/>
          <soapenv:Body>
            <ec:consultarEstadoAutorizacionComprobante>
              <claveAcceso>%s</claveAcceso>
            </ec:consultarEstadoAutorizacionComprobante>
          </soapenv:Body>
        </soapenv:Envelope>
        """.formatted(claveAcceso);
    HttpResponse<String> response = sendSoap(endpoints.getConsultaComprobanteUrl(), envelope, "");
    if (response == null) {
      return SriConsultaAutorizacionResponse.error("No se pudo conectar a SRI");
    }
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      return SriConsultaAutorizacionResponse.error("Error HTTP consulta: " + response.statusCode());
    }
    return parseConsultaComprobanteResponse(response.body());
  }

  @Override
  public SriConsultaFacturaResponse consultarEstadoFacturaComercial(Ambiente ambiente, String claveAcceso) {
    if (isBlank(claveAcceso)) {
      return SriConsultaFacturaResponse.error("Clave de acceso requerida para consulta");
    }
    AppProperties.Endpoints endpoints = endpointsFor(ambiente);
    if (endpoints == null || isBlank(endpoints.getConsultaFacturaUrl())) {
      return SriConsultaFacturaResponse.error("Endpoint de consulta factura no configurado");
    }
    String envelope = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ec="http://ec.gob.sri.ws.consultas">
          <soapenv:Header/>
          <soapenv:Body>
            <ec:consultarEstadoConfirmacionFacturaComercialNegociable>
              <claveAcceso>%s</claveAcceso>
            </ec:consultarEstadoConfirmacionFacturaComercialNegociable>
          </soapenv:Body>
        </soapenv:Envelope>
        """.formatted(claveAcceso);
    HttpResponse<String> response = sendSoap(endpoints.getConsultaFacturaUrl(), envelope, "");
    if (response == null) {
      return SriConsultaFacturaResponse.error("No se pudo conectar a SRI");
    }
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      return SriConsultaFacturaResponse.error("Error HTTP consulta factura: " + response.statusCode());
    }
    return parseConsultaFacturaResponse(response.body());
  }

  @Override
  public SriResponse enviarLote(Ambiente ambiente, String claveAccesoLote, String rucEmisor, List<String> xmlsFirmados) {
    AppProperties.Endpoints endpoints = endpointsFor(ambiente);
    if (endpoints == null || isBlank(endpoints.getRecepcionUrl())) {
      return SriResponse.error("Endpoint de recepcion no configurado");
    }
    String loteXml = loteXmlBuilder.build(claveAccesoLote, rucEmisor, xmlsFirmados);
    return enviarRecepcion(endpoints.getRecepcionUrl(), loteXml);
  }

  @Override
  public SriLoteAutorizacionResponse consultarAutorizacionLote(Ambiente ambiente, String claveAccesoLote) {
    if (isBlank(claveAccesoLote)) {
      return SriLoteAutorizacionResponse.error("Clave de acceso de lote requerida");
    }
    AppProperties.Endpoints endpoints = endpointsFor(ambiente);
    if (endpoints == null || isBlank(endpoints.getAutorizacionUrl())) {
      return SriLoteAutorizacionResponse.error("Endpoint de autorizacion no configurado");
    }
    String envelope = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ec="http://ec.gob.sri.ws.autorizacion">
          <soapenv:Header/>
          <soapenv:Body>
            <ec:autorizacionComprobanteLote>
              <claveAccesoLote>%s</claveAccesoLote>
            </ec:autorizacionComprobanteLote>
          </soapenv:Body>
        </soapenv:Envelope>
        """.formatted(claveAccesoLote);
    HttpResponse<String> response = sendSoap(endpoints.getAutorizacionUrl(), envelope, "");
    if (response == null) {
      return SriLoteAutorizacionResponse.error("No se pudo conectar a SRI");
    }
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      return SriLoteAutorizacionResponse.error("Error HTTP autorizacion lote: " + response.statusCode());
    }
    return parseAutorizacionLoteResponse(response.body(), claveAccesoLote);
  }

  private AppProperties.Endpoints endpointsFor(Ambiente ambiente) {
    if (ambiente == Ambiente.PRODUCCION) {
      return properties.getSri().getProduccion();
    }
    return properties.getSri().getPruebas();
  }

  private SriResponse enviarRecepcion(String url, String xmlFirmado) {
    String payload = Base64.getEncoder().encodeToString(xmlFirmado.getBytes(StandardCharsets.UTF_8));
    String envelope = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ec="http://ec.gob.sri.ws.recepcion">
          <soapenv:Header/>
          <soapenv:Body>
            <ec:validarComprobante>
              <xml>%s</xml>
            </ec:validarComprobante>
          </soapenv:Body>
        </soapenv:Envelope>
        """.formatted(payload);
    HttpResponse<String> response = sendSoap(url, envelope, "");
    if (response == null) {
      return SriResponse.error("No se pudo conectar a SRI");
    }
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      String fault = soapFaultMessage(response.body());
      if (!isBlank(fault)) {
        return SriResponse.error("Error recepcion SRI: " + fault);
      }
      return SriResponse.error("Error HTTP recepcion: " + response.statusCode());
    }
    return parseRecepcionResponse(response.body());
  }

  private SriResponse consultarAutorizacion(String url, String claveAcceso) {
    String envelope = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ec="http://ec.gob.sri.ws.autorizacion">
          <soapenv:Header/>
          <soapenv:Body>
            <ec:autorizacionComprobante>
              <claveAccesoComprobante>%s</claveAccesoComprobante>
            </ec:autorizacionComprobante>
          </soapenv:Body>
        </soapenv:Envelope>
        """.formatted(claveAcceso);
    HttpResponse<String> response = sendSoap(url, envelope, "");
    if (response == null) {
      return SriResponse.error("No se pudo conectar a SRI");
    }
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      String fault = soapFaultMessage(response.body());
      if (!isBlank(fault)) {
        return SriResponse.error("Error autorizacion SRI: " + fault);
      }
      return SriResponse.error("Error HTTP autorizacion: " + response.statusCode());
    }
    return parseAutorizacionResponse(response.body(), claveAcceso);
  }

  // Reintenta solo cuando el SRI indica procesamiento asincrono.
  public SriResponse consultarAutorizacionConReintentos(String url, String claveAcceso) {
    if (isBlank(url)) {
      return SriResponse.error("Endpoint de autorizacion no configurado");
    }
    if (isBlank(claveAcceso)) {
      return SriResponse.error("Clave de acceso requerida para autorizacion");
    }
    int maxIntentos = Math.max(1, properties.getSri().getAutorizacionMaxIntentos());
    for (int intento = 1; intento <= maxIntentos; intento++) {
      SriResponse response = consultarAutorizacion(url, claveAcceso);
      if (response.status() != SriResponseStatus.EN_PROCESO) {
        return response;
      }
      if (intento == maxIntentos) {
        return response;
      }
      long backoffSeconds = backoffSecondsForAttempt(intento);
      if (!sleepBackoffSeconds(backoffSeconds)) {
        return SriResponse.error("Interrumpido durante espera de autorizacion SRI");
      }
    }
    return SriResponse.enProceso();
  }

  private HttpResponse<String> sendSoap(String url, String body, String soapAction) {
    try {
      HttpRequest.Builder builder = HttpRequest.newBuilder()
          .uri(URI.create(normalizeSoapEndpoint(url)))
          .timeout(Duration.ofSeconds(properties.getSri().getTimeoutSeconds()))
          .header("Content-Type", "text/xml; charset=UTF-8");
      if (!isBlank(soapAction)) {
        builder.header("SOAPAction", "\"" + soapAction + "\"");
      }
      HttpRequest request = builder
          .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
          .build();
      return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (Exception ex) {
      return null;
    }
  }

  private String normalizeSoapEndpoint(String url) {
    if (isBlank(url)) {
      return url;
    }
    String lower = url.toLowerCase(Locale.ROOT);
    int wsdlIndex = lower.indexOf("?wsdl");
    if (wsdlIndex < 0) {
      return url;
    }
    return url.substring(0, wsdlIndex);
  }

  private long backoffSecondsForAttempt(int intento) {
    long base = properties.getSri().getAutorizacionBackoffSeconds();
    long max = properties.getSri().getAutorizacionBackoffMaxSeconds();
    if (base <= 0) {
      base = 5;
    }
    if (max < base) {
      max = base;
    }
    long value = base;
    for (int i = 1; i < intento; i++) {
      if (value >= max) {
        return max;
      }
      value = Math.min(max, value * 2);
    }
    return value;
  }

  private boolean sleepBackoffSeconds(long seconds) {
    if (seconds <= 0) {
      return true;
    }
    try {
      Thread.sleep(Duration.ofSeconds(seconds).toMillis());
      return true;
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private SriResponse parseRecepcionResponse(String xml) {
    Document doc = parseXml(xml);
    if (doc == null) {
      return SriResponse.error("Respuesta recepcion invalida");
    }
    String fault = soapFaultMessage(doc);
    if (!isBlank(fault)) {
      return SriResponse.error("Fault SOAP recepcion: " + fault);
    }
    String estado = firstElementText(doc, "estado");
    if (estado == null) {
      return SriResponse.error("Respuesta recepcion sin estado");
    }
    String normalized = estado.trim().toUpperCase(Locale.ROOT);
    if ("RECIBIDA".equals(normalized)) {
      return SriResponse.enProceso();
    }
    if ("DEVUELTA".equals(normalized)) {
      if (isProcesamientoSri(doc)) {
        return SriResponse.enProceso();
      }
      return SriResponse.noAutorizado(mensajesOrDefault(doc, "Comprobante devuelto por SRI"));
    }
    if ("RECHAZADO".equals(normalized) || "RECHAZADA".equals(normalized)) {
      return SriResponse.noAutorizado(mensajesOrDefault(doc, "Comprobante devuelto por SRI"));
    }
    return SriResponse.error("Estado recepcion desconocido: " + estado);
  }

  private SriResponse parseAutorizacionResponse(String xml, String claveAcceso) {
    Document doc = parseXml(xml);
    if (doc == null) {
      return SriResponse.error("Respuesta autorizacion invalida");
    }
    String fault = soapFaultMessage(doc);
    if (!isBlank(fault)) {
      return SriResponse.error("Fault SOAP autorizacion: " + fault);
    }
    String estado = firstElementText(doc, "estado");
    if (estado == null) {
      return SriResponse.error("Respuesta autorizacion sin estado");
    }
    String normalized = estado.trim().toUpperCase(Locale.ROOT);
    if ("AUTORIZADO".equals(normalized)) {
      String numero = firstElementText(doc, "numeroAutorizacion");
      if (isBlank(numero)) {
        numero = claveAcceso;
      }
      return SriResponse.autorizado(numero);
    }
    if (isProcesamientoSri(doc)) {
      return SriResponse.enProceso();
    }
    if ("NO AUTORIZADO".equals(normalized) || "RECHAZADO".equals(normalized) || "RECHAZADA".equals(normalized)) {
      return SriResponse.noAutorizado(mensajesOrDefault(doc, "Comprobante no autorizado por SRI"));
    }
    if ("EN PROCESAMIENTO".equals(normalized) || "EN PROCESO".equals(normalized)) {
      return SriResponse.enProceso();
    }
    return SriResponse.error("Estado autorizacion desconocido: " + estado);
  }

  private SriConsultaAutorizacionResponse parseConsultaComprobanteResponse(String xml) {
    Document doc = parseXml(xml);
    if (doc == null) {
      return SriConsultaAutorizacionResponse.error("Respuesta consulta invalida");
    }
    String estadoConsulta = firstElementText(doc, "estadoConsulta");
    String estadoAutorizacion = firstElementText(doc, "estadoAutorizacion");
    SriConsultaAutorizacionStatus status = mapConsultaAutorizacionStatus(doc, estadoConsulta, estadoAutorizacion);
    String mensaje = mensajesOrDefault(doc, null);
    if (status == SriConsultaAutorizacionStatus.ERROR) {
      return SriConsultaAutorizacionResponse.error(mensaje);
    }
    return new SriConsultaAutorizacionResponse(
        status,
        estadoConsulta,
        estadoAutorizacion,
        firstElementText(doc, "claveAcceso"),
        firstElementText(doc, "rucEmisor"),
        firstElementText(doc, "tipoComprobante"),
        firstElementText(doc, "fechaAutorizacion"),
        mensaje
    );
  }

  private SriConsultaFacturaResponse parseConsultaFacturaResponse(String xml) {
    Document doc = parseXml(xml);
    if (doc == null) {
      return SriConsultaFacturaResponse.error("Respuesta consulta factura invalida");
    }
    String estadoConsulta = firstElementText(doc, "estadoConsulta");
    String estadoConfirmacion = firstElementText(doc, "estadoConfirmacion");
    SriConsultaFacturaStatus status = mapConsultaFacturaStatus(estadoConsulta, estadoConfirmacion);
    String mensaje = mensajesOrDefault(doc, null);
    if (status == SriConsultaFacturaStatus.ERROR) {
      return SriConsultaFacturaResponse.error(mensaje);
    }
    return new SriConsultaFacturaResponse(
        status,
        estadoConsulta,
        estadoConfirmacion,
        firstElementText(doc, "claveAcceso"),
        mensaje
    );
  }

  private SriLoteAutorizacionResponse parseAutorizacionLoteResponse(String xml, String claveAccesoLote) {
    Document doc = parseXml(xml);
    if (doc == null) {
      return SriLoteAutorizacionResponse.error("Respuesta autorizacion lote invalida");
    }
    String clave = firstElementText(doc, "claveAccesoLoteConsultada");
    if (isBlank(clave)) {
      clave = claveAccesoLote;
    }
    NodeList autorizaciones = doc.getElementsByTagNameNS("*", "autorizacion");
    List<SriLoteAutorizacionItem> items = new ArrayList<>();
    for (int i = 0; i < autorizaciones.getLength(); i++) {
      Node node = autorizaciones.item(i);
      if (!(node instanceof Element element)) {
        continue;
      }
      String estado = textFromDirectChild(element, "estado");
      String numero = textFromDirectChild(element, "numeroAutorizacion");
      String comprobanteXml = textFromDirectChild(element, "comprobante");
      String claveAcceso = extractClaveAcceso(comprobanteXml);
      String mensaje = mensajesOrDefault(element, null);
      items.add(new SriLoteAutorizacionItem(claveAcceso, estado, numero, mensaje));
    }
    if (items.isEmpty()) {
      return new SriLoteAutorizacionResponse(clave, items, mensajesOrDefault(doc, "Sin autorizaciones en lote"));
    }
    return new SriLoteAutorizacionResponse(clave, items, mensajesOrDefault(doc, null));
  }

    private SriConsultaAutorizacionStatus mapConsultaAutorizacionStatus(
            Document doc,
            String estadoConsulta,
            String estadoAutorizacion
    ) {
        if (isBlank(estadoConsulta) && isBlank(estadoAutorizacion)) {
            return SriConsultaAutorizacionStatus.ERROR;
        }

        // 1️⃣ Manejo especial: RECHAZADA por error temporal SRI (id 99)
        if (!isBlank(estadoConsulta)) {
            String normalized = estadoConsulta.trim().toUpperCase(Locale.ROOT);
            if ("RECHAZADA".equals(normalized) || "RECHAZADO".equals(normalized)) {
                if (isErrorConsultaTemporal(doc)) {
                    // 👈 ESTE es el cambio clave
                    return SriConsultaAutorizacionStatus.DESCONOCIDO;
                }
                return SriConsultaAutorizacionStatus.RECHAZADA;
            }
        }

        // 2️⃣ Si aún no hay estado de autorización
        if (isBlank(estadoAutorizacion)) {
            return SriConsultaAutorizacionStatus.DESCONOCIDO;
        }

        String normalized = estadoAutorizacion.trim().toUpperCase(Locale.ROOT);

        if ("EN PROCESAMIENTO".equals(normalized) || "EN PROCESO".equals(normalized)) {
            return SriConsultaAutorizacionStatus.DESCONOCIDO;
        }

        return switch (normalized) {
            case "AUTORIZADO" -> SriConsultaAutorizacionStatus.AUTORIZADO;
            case "NO AUTORIZADO" -> SriConsultaAutorizacionStatus.NO_AUTORIZADO;
            case "PENDIENTE DE ANULAR" -> SriConsultaAutorizacionStatus.PENDIENTE_ANULAR;
            case "ANULADO" -> SriConsultaAutorizacionStatus.ANULADO;
            default -> SriConsultaAutorizacionStatus.DESCONOCIDO;
        };
    }
    private boolean isErrorConsultaTemporal(Document doc) {
        NodeList mensajes = doc.getElementsByTagNameNS("*", "mensaje");
        for (int i = 0; i < mensajes.getLength(); i++) {
            Node node = mensajes.item(i);
            if (!(node instanceof Element m)) {
                continue;
            }
            String id = textFromDirectChild(m, "identificador");
            String info = textFromDirectChild(m, "informacionAdicional");

            if ("99".equals(id)
                    && info != null
                    && info.toUpperCase(Locale.ROOT).contains("NO EXISTEN DATOS")) {
                return true;
            }
        }
        return false;
    }




    private SriConsultaFacturaStatus mapConsultaFacturaStatus(String estadoConsulta, String estadoConfirmacion) {
    if (isBlank(estadoConsulta) && isBlank(estadoConfirmacion)) {
      return SriConsultaFacturaStatus.ERROR;
    }
    if (!isBlank(estadoConsulta)) {
      String normalized = estadoConsulta.trim().toUpperCase(Locale.ROOT);
      if ("RECHAZADA".equals(normalized) || "RECHAZADO".equals(normalized)) {
        return SriConsultaFacturaStatus.RECHAZADA;
      }
    }
    if (isBlank(estadoConfirmacion)) {
      return SriConsultaFacturaStatus.DESCONOCIDO;
    }
    String normalized = estadoConfirmacion.trim().toUpperCase(Locale.ROOT);
    if ("SI".equals(normalized)) {
      return SriConsultaFacturaStatus.SI;
    }
    if ("RECHAZADA".equals(normalized) || "NO".equals(normalized)) {
      return SriConsultaFacturaStatus.RECHAZADA;
    }
    return SriConsultaFacturaStatus.DESCONOCIDO;
  }

  private String extractClaveAcceso(String comprobanteXml) {
    if (isBlank(comprobanteXml)) {
      return null;
    }
    Document doc = parseXml(comprobanteXml);
    if (doc == null) {
      return null;
    }
    return firstElementText(doc, "claveAcceso");
  }

  private Document parseXml(String xml) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setExpandEntityReferences(false);
      return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    } catch (Exception ex) {
      return null;
    }
  }

  private String soapFaultMessage(String xml) {
    if (isBlank(xml)) {
      return null;
    }
    Document doc = parseXml(xml);
    if (doc == null) {
      return null;
    }
    return soapFaultMessage(doc);
  }

  private String soapFaultMessage(Document doc) {
    String faultString = firstElementText(doc, "faultstring");
    if (isBlank(faultString)) {
      return null;
    }
    String faultCode = firstElementText(doc, "faultcode");
    if (isBlank(faultCode)) {
      return faultString;
    }
    return faultCode + ": " + faultString;
  }

  private String firstElementText(Document doc, String localName) {
    NodeList nodes = doc.getElementsByTagNameNS("*", localName);
    for (int i = 0; i < nodes.getLength(); i++) {
      String text = nodes.item(i).getTextContent();
      if (text != null) {
        String trimmed = text.trim();
        if (!trimmed.isEmpty()) {
          return trimmed;
        }
      }
    }
    return null;
  }

  private String mensajesOrDefault(Node scope, String fallback) {
    String mensajes = collectMensajes(scope);
    if (isBlank(mensajes)) {
      return fallback;
    }
    return mensajes;
  }

  private String collectMensajes(Node scope) {
    NodeList nodes = messageNodes(scope);
    if (nodes == null) {
      return null;
    }
    List<String> mensajes = new ArrayList<>();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (!(node instanceof Element element)) {
        continue;
      }
      if (!hasChildElements(element)) {
        continue;
      }
      String identificador = textFromDirectChild(element, "identificador");
      String texto = textFromDirectChild(element, "mensaje");
      String tipo = textFromDirectChild(element, "tipo");
      if (isBlank(texto)) {
        continue;
      }
      StringBuilder builder = new StringBuilder();
      if (!isBlank(identificador)) {
        builder.append(identificador.trim()).append(": ");
      }
      builder.append(texto.trim());
      if (!isBlank(tipo)) {
        builder.append(" (").append(tipo.trim()).append(")");
      }
      mensajes.add(builder.toString());
    }
    if (mensajes.isEmpty()) {
      return null;
    }
    return String.join(" | ", mensajes);
  }

  private NodeList messageNodes(Node scope) {
    if (scope instanceof Document doc) {
      return doc.getElementsByTagNameNS("*", "mensaje");
    }
    if (scope instanceof Element element) {
      return element.getElementsByTagNameNS("*", "mensaje");
    }
    return null;
  }

  private boolean hasChildElements(Element element) {
    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
        return true;
      }
    }
    return false;
  }

  private String textFromDirectChild(Element parent, String localName) {
    NodeList children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE && localName.equals(child.getLocalName())) {
        String text = child.getTextContent();
        return text == null ? null : text.trim();
      }
    }
    return null;
  }

  private boolean isProcesamientoSri(Document doc) {
    NodeList nodes = messageNodes(doc);
    if (nodes == null) {
      return false;
    }
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (!(node instanceof Element element)) {
        continue;
      }
      if (!hasChildElements(element)) {
        continue;
      }
      String identificador = textFromDirectChild(element, "identificador");
      if ("70".equals(trimToNull(identificador))) {
        return true;
      }
      String mensaje = textFromDirectChild(element, "mensaje");
      String info = textFromDirectChild(element, "informacionAdicional");
      if (containsProcesamiento(mensaje) || containsProcesamiento(info)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsProcesamiento(String value) {
    if (isBlank(value)) {
      return false;
    }
    String normalized = value.trim().toUpperCase(Locale.ROOT);
    return normalized.contains("PROCESAMIENTO");
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
