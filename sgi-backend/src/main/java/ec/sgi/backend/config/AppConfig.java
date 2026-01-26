package ec.sgi.backend.config;

import ec.sgi.backend.domain.service.FacturaTotalsCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
  @Bean
  public FacturaTotalsCalculator facturaTotalsCalculator() {
    return new FacturaTotalsCalculator();
  }
}
