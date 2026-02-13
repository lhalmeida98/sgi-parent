package ec.sgi.backend.infrastructure.sri;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.sgi.backend.application.dto.SriContribuyenteInfo;
import ec.sgi.backend.application.port.out.SriContribuyentePort;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SriContribuyenteApiClient implements SriContribuyentePort {
  private static final Logger log = LoggerFactory.getLogger(SriContribuyenteApiClient.class);
  private static final TypeReference<List<SriContribuyenteResponse>> LIST_TYPE = new TypeReference<>() {};

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final boolean enabled;
  private final String baseUrl;
  private final Duration timeout;

  public SriContribuyenteApiClient(
      ObjectMapper objectMapper,
      @Value("${app.sri.contribuyente.enabled:true}") boolean enabled,
      @Value("${app.sri.contribuyente.base-url:https://srienlinea.sri.gob.ec/sri-catastro-sujeto-servicio-internet/rest/ConsolidadoContribuyente/obtenerPorNumerosRuc}") String baseUrl,
      @Value("${app.sri.contribuyente.timeout-ms:4000}") long timeoutMs
  ) {
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = objectMapper;
    this.enabled = enabled;
    this.baseUrl = baseUrl;
    this.timeout = Duration.ofMillis(timeoutMs);
  }

  @Override
  public Optional<SriContribuyenteInfo> consultarPorRuc(String ruc) {
    if (!enabled || ruc == null || ruc.isBlank()) {
      return Optional.empty();
    }
    try {
      String encoded = URLEncoder.encode(ruc.trim(), StandardCharsets.UTF_8);
      String url = baseUrl + "?&ruc=" + encoded;
      HttpRequest request = HttpRequest.newBuilder(URI.create(url))
          .GET()
          .timeout(timeout)
          .header("Accept", "application/json")
          .build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        throw new IllegalStateException("Respuesta SRI HTTP " + response.statusCode());
      }
      List<SriContribuyenteResponse> items = objectMapper.readValue(response.body(), LIST_TYPE);
      if (items == null || items.isEmpty()) {
        return Optional.empty();
      }
      SriContribuyenteResponse item = items.get(0);
      return Optional.of(new SriContribuyenteInfo(
          item.numeroRuc(),
          item.razonSocial(),
          item.estadoContribuyenteRuc(),
          item.actividadEconomicaPrincipal(),
          item.tipoContribuyente(),
          item.regimen(),
          item.categoria(),
          item.obligadoLlevarContabilidad(),
          item.agenteRetencion(),
          item.contribuyenteEspecial(),
          item.contribuyenteFantasma(),
          item.transaccionesInexistente()
      ));
    } catch (Exception ex) {
      log.warn("No se pudo consultar SRI para RUC {}: {}", ruc, ex.getMessage());
      throw new IllegalStateException("No se pudo consultar SRI");
    }
  }

  private record SriContribuyenteResponse(
      String numeroRuc,
      String razonSocial,
      String estadoContribuyenteRuc,
      String actividadEconomicaPrincipal,
      String tipoContribuyente,
      String regimen,
      String categoria,
      String obligadoLlevarContabilidad,
      String agenteRetencion,
      String contribuyenteEspecial,
      String contribuyenteFantasma,
      String transaccionesInexistente
  ) {
  }
}
