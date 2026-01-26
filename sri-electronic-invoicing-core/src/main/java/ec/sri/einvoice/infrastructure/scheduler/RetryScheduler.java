package ec.sri.einvoice.infrastructure.scheduler;

import ec.sri.einvoice.application.port.in.ReintentarEnvioUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

    @Component
    public class RetryScheduler {
      private final ReintentarEnvioUseCase useCase;

      public RetryScheduler(ReintentarEnvioUseCase useCase) {
        this.useCase = useCase;
      }

      /*@Scheduled(fixedDelayString = "${app.retry.scheduler-ms:60000}")
      public void ejecutar() {
        useCase.reintentarPendientes();
      }*/
}
