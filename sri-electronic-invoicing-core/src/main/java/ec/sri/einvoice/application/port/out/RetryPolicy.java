package ec.sri.einvoice.application.port.out;

import ec.sri.einvoice.domain.model.Comprobante;
import java.time.Instant;

public interface RetryPolicy {
  boolean shouldRetry(Comprobante comprobante, Instant ahora);
  Instant nextAttemptTime(int attempts, Instant ahora);
  boolean canRetry(int attempts);
}
