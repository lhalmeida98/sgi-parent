package ec.sri.einvoice.infrastructure.time;

import ec.sri.einvoice.application.port.out.TimeProvider;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SystemTimeProvider implements TimeProvider {
  @Override
  public Instant now() {
    return Instant.now();
  }
}
