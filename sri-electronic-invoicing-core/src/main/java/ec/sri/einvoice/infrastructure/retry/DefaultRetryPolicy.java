package ec.sri.einvoice.infrastructure.retry;

import ec.sri.einvoice.application.port.out.RetryPolicy;
import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.infrastructure.config.AppProperties;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class DefaultRetryPolicy implements RetryPolicy {
  private final AppProperties properties;

  public DefaultRetryPolicy(AppProperties properties) {
    this.properties = properties;
  }

  @Override
  public boolean shouldRetry(Comprobante comprobante, Instant ahora) {
    if (!canRetry(comprobante.intentosEnvio())) {
      return false;
    }
    if (comprobante.siguienteReintento() == null) {
      return true;
    }
    return !comprobante.siguienteReintento().isAfter(ahora);
  }

  @Override
  public Instant nextAttemptTime(int attempts, Instant ahora) {
    long backoff = properties.getRetry().getBackoffSeconds();
    return ahora.plusSeconds(backoff * (attempts + 1L));
  }

  @Override
  public boolean canRetry(int attempts) {
    return attempts < properties.getRetry().getMaxAttempts();
  }
}
