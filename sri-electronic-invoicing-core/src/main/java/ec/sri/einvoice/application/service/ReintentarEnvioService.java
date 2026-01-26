package ec.sri.einvoice.application.service;

import ec.sri.einvoice.application.port.in.ReintentarEnvioUseCase;
import ec.sri.einvoice.application.port.out.AuditLogRepository;
import ec.sri.einvoice.application.port.out.ComprobanteRepository;
import ec.sri.einvoice.application.port.out.EventStore;
import ec.sri.einvoice.application.port.out.RetryPolicy;
import ec.sri.einvoice.application.port.out.SriClient;
import ec.sri.einvoice.application.port.out.SriResponse;
import ec.sri.einvoice.application.port.out.SriResponseStatus;
import ec.sri.einvoice.application.port.out.TimeProvider;
import ec.sri.einvoice.domain.model.BitacoraEntry;
import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.domain.model.EstadoComprobante;
import ec.sri.einvoice.domain.service.ComprobanteStateMachine;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ReintentarEnvioService implements ReintentarEnvioUseCase {
  private final ComprobanteRepository repository;
  private final EventStore eventStore;
  private final AuditLogRepository auditLogRepository;
  private final SriClient sriClient;
  private final RetryPolicy retryPolicy;
  private final ComprobanteStateMachine stateMachine;
  private final TimeProvider timeProvider;

  public ReintentarEnvioService(
      ComprobanteRepository repository,
      EventStore eventStore,
      AuditLogRepository auditLogRepository,
      SriClient sriClient,
      RetryPolicy retryPolicy,
      ComprobanteStateMachine stateMachine,
      TimeProvider timeProvider
  ) {
    this.repository = repository;
    this.eventStore = eventStore;
    this.auditLogRepository = auditLogRepository;
    this.sriClient = sriClient;
    this.retryPolicy = retryPolicy;
    this.stateMachine = stateMachine;
    this.timeProvider = timeProvider;
  }

  @Override
  public void reintentarPendientes() {
    Instant ahora = timeProvider.now();
    List<Comprobante> pendientes = repository.findPendientes(ahora);
        for (Comprobante comprobante : pendientes) {
          if (!retryPolicy.shouldRetry(comprobante, ahora)) {
            continue;
          }
          if (comprobante.estado() == EstadoComprobante.ERROR) {
            stateMachine.transition(comprobante, EstadoComprobante.EN_COLA, ahora, "Reprogramado para envio");
          }
          stateMachine.transition(comprobante, EstadoComprobante.ENVIADO, ahora, "Enviado a SRI");
          SriResponse response = sriClient.enviar(comprobante, comprobante.xmlFirmado());
          if (response.status() == SriResponseStatus.AUTORIZADO) {
            comprobante.registrarAutorizacion(response.numeroAutorizacion());
            stateMachine.transition(comprobante, EstadoComprobante.AUTORIZADO, ahora, "Autorizado por SRI");
          } else if (response.status() == SriResponseStatus.NO_AUTORIZADO) {
            comprobante.registrarRechazo(response.mensaje());
            stateMachine.transition(comprobante, EstadoComprobante.RECHAZADO, ahora, "Rechazado por SRI");
          } else if (response.status() == SriResponseStatus.ENVIADO_SRI
              || response.status() == SriResponseStatus.EN_PROCESO) {
            // En espera de autorizacion; no re-enviar el comprobante.
          } else {
            comprobante.incrementarIntento(ahora, retryPolicy.nextAttemptTime(comprobante.intentosEnvio(), ahora));
            stateMachine.transition(comprobante, EstadoComprobante.ERROR, ahora, "Error de envio");
          }

      repository.save(comprobante);
      eventStore.append(comprobante.pullEvents());
      auditLogRepository.save(new BitacoraEntry(UUID.randomUUID(), comprobante.id(), "REINTENTO", "Reintento de envio", ahora));
    }
  }
}
