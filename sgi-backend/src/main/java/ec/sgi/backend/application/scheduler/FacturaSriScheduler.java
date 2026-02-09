package ec.sgi.backend.application.scheduler;

import ec.sgi.backend.application.service.FacturaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.facturas.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FacturaSriScheduler {
  private static final Logger log = LoggerFactory.getLogger(FacturaSriScheduler.class);

  private final FacturaService facturaService;

  public FacturaSriScheduler(
      FacturaService facturaService
  ) {
    this.facturaService = facturaService;
  }

  @Scheduled(fixedDelayString = "${app.facturas.scheduler.fixed-delay-ms:15000}")
  public void consultarFacturasEnProceso() {
    int procesadas = facturaService.procesarFacturasEnProceso();
    if (procesadas > 0) {
      log.info("Scheduler SRI procesó {} facturas en proceso", procesadas);
    } else {
      log.debug("Scheduler SRI sin facturas en proceso");
    }
  }
}
