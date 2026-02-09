package ec.sgi.backend.infrastructure.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.sgi.backend.application.exception.EmailSendException;
import ec.sgi.backend.application.port.EmailAttachment;
import ec.sgi.backend.application.port.EmailService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResendEmailService implements EmailService {
  private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);
  private static final URI RESEND_EMAILS_URI = URI.create("https://api.resend.com/emails");

  private final String apiKey;
  private final String fromEmail;
  private final String fromName;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public ResendEmailService(
      @Value("${RESEND_API_KEY:}") String apiKey,
      @Value("${mail.from.email:}") String fromEmail,
      @Value("${mail.from.name:}") String fromName,
      ObjectMapper objectMapper
  ) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new EmailSendException("RESEND_API_KEY no configurada");
    }
    if (fromEmail == null || fromEmail.isBlank()) {
      throw new EmailSendException("mail.from.email no configurado");
    }
    this.apiKey = apiKey;
    this.fromEmail = fromEmail;
    this.fromName = fromName == null ? "" : fromName.trim();
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newHttpClient();
  }

  @Override
  public void enviarEmail(String to, String subject, String htmlBody, List<EmailAttachment> attachments) {
    try {
      List<Map<String, String>> payloadAttachments = attachments == null
          ? List.of()
          : attachments.stream()
              .map(this::toAttachmentPayload)
              .toList();
      String payload = buildPayload(to, subject, htmlBody, payloadAttachments);

      HttpRequest request = HttpRequest.newBuilder(RESEND_EMAILS_URI)
          .header("Authorization", "Bearer " + apiKey)
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(payload))
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      int status = response.statusCode();
      if (status < 200 || status >= 300) {
        log.error("Resend error status={} body={}", status, response.body());
        throw new EmailSendException("No se pudo enviar el correo");
      }
    } catch (EmailSendException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("Error enviando correo con Resend", ex);
      throw new EmailSendException("No se pudo enviar el correo");
    }
  }

  private String buildPayload(String to, String subject, String htmlBody, List<Map<String, String>> attachments)
      throws JsonProcessingException {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("from", formatFrom());
    payload.put("to", List.of(to));
    payload.put("subject", subject);
    payload.put("html", htmlBody);
    if (!attachments.isEmpty()) {
      payload.put("attachments", attachments);
    }
    return objectMapper.writeValueAsString(payload);
  }

  private String formatFrom() {
    if (fromName.isBlank()) {
      return fromEmail;
    }
    return fromName + " <" + fromEmail + ">";
  }

  private Map<String, String> toAttachmentPayload(EmailAttachment attachment) {
    String filename = attachment.filename();
    if (filename == null || filename.isBlank()) {
      throw new EmailSendException("Adjunto sin nombre de archivo");
    }
    String contenido = normalizeBase64(attachment.base64Content());
    return Map.of(
        "filename", filename,
        "content", contenido
    );
  }

  private String normalizeBase64(String value) {
    String trimmed = value == null ? "" : value.trim();
    if (trimmed.startsWith("data:")) {
      int index = trimmed.indexOf(",");
      if (index >= 0 && index + 1 < trimmed.length()) {
        trimmed = trimmed.substring(index + 1);
      }
    }
    byte[] decoded = Base64.getDecoder().decode(trimmed);
    return Base64.getEncoder().encodeToString(decoded);
  }
}
