package ec.sgi.backend.infrastructure.email;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MailConfigHealthCheck implements SmartInitializingSingleton {
  private static final Logger log = LoggerFactory.getLogger(MailConfigHealthCheck.class);

  private final String apiKey;
  private final String fromEmail;
  private final String fromName;

  public MailConfigHealthCheck(
      @Value("${RESEND_API_KEY:}") String apiKey,
      @Value("${mail.from.email:}") String fromEmail,
      @Value("${mail.from.name:}") String fromName
  ) {
    this.apiKey = apiKey;
    this.fromEmail = fromEmail;
    this.fromName = fromName;
  }

  @Override
  public void afterSingletonsInstantiated() {
    List<String> missing = new ArrayList<>();
    if (isBlank(apiKey)) {
      missing.add("RESEND_API_KEY");
    }
    if (!missing.isEmpty()) {
      throw new IllegalStateException("Configuracion de correo incompleta: " + String.join(", ", missing));
    }
    if (isBlank(fromEmail)) {
      log.warn("mail.from.email vacio, Resend requiere un remitente valido.");
    } else if (fromEmail.toLowerCase().endsWith("@resend.dev")) {
      log.warn("Usando remitente de prueba @resend.dev; solo permite envios a tu email verificado.");
    }
    if (isBlank(fromName)) {
      log.info("mail.from.name no configurado, se enviara sin nombre.");
    }
    log.info("Configuracion de correo Resend verificada.");
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
