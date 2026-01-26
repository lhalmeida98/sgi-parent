package ec.sri.einvoice.infrastructure.retry;

import ec.sri.einvoice.application.port.out.OfflineModePolicy;
import ec.sri.einvoice.infrastructure.config.AppProperties;
import org.springframework.stereotype.Component;

@Component
public class ConfigOfflineModePolicy implements OfflineModePolicy {
  private final AppProperties properties;

  public ConfigOfflineModePolicy(AppProperties properties) {
    this.properties = properties;
  }

  @Override
  public boolean isOfflineEnabled() {
    return properties.getOffline().isEnabled();
  }
}
