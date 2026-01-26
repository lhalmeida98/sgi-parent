package ec.sri.einvoice.application.service;

import ec.sri.einvoice.application.port.in.EmitirComprobanteCommand;
import ec.sri.einvoice.application.port.in.EmitirComprobanteUseCase;
import ec.sri.einvoice.application.port.out.AuditLogRepository;
import ec.sri.einvoice.application.port.out.ComprobanteRepository;
import ec.sri.einvoice.application.port.out.DigitalSignatureService;
import ec.sri.einvoice.application.port.out.EventStore;
import ec.sri.einvoice.application.port.out.OfflineModePolicy;
import ec.sri.einvoice.application.port.out.RetryPolicy;
import ec.sri.einvoice.application.port.out.SriClient;
import ec.sri.einvoice.application.port.out.SriResponse;
import ec.sri.einvoice.application.port.out.SriResponseStatus;
import ec.sri.einvoice.application.port.out.SriXmlValidator;
import ec.sri.einvoice.application.port.out.TimeProvider;
import ec.sri.einvoice.application.port.out.XmlComprobanteGenerator;
import ec.sri.einvoice.domain.model.BitacoraEntry;
import ec.sri.einvoice.domain.model.ClaveAcceso;
import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.domain.model.ComprobanteId;
import ec.sri.einvoice.domain.model.EstadoComprobante;
import ec.sri.einvoice.domain.service.ClaveAccesoGenerator;
import ec.sri.einvoice.domain.service.ComprobanteStateMachine;
import ec.sri.einvoice.domain.service.ComprobanteValidator;
import java.time.Instant;
import java.util.UUID;

public class EmitirComprobanteService implements EmitirComprobanteUseCase {
  private final ComprobanteRepository repository;
  private final EventStore eventStore;
  private final AuditLogRepository auditLogRepository;
  private final ComprobanteValidator validator;
  private final ComprobanteStateMachine stateMachine;
  private final ClaveAccesoGenerator claveAccesoGenerator;
  private final XmlComprobanteGenerator xmlGenerator;
  private final SriXmlValidator xmlValidator;
  private final DigitalSignatureService signatureService;
  private final OfflineModePolicy offlineModePolicy;
  private final RetryPolicy retryPolicy;
  private final SriClient sriClient;
  private final TimeProvider timeProvider;

  public EmitirComprobanteService(
      ComprobanteRepository repository,
      EventStore eventStore,
      AuditLogRepository auditLogRepository,
      ComprobanteValidator validator,
      ComprobanteStateMachine stateMachine,
      ClaveAccesoGenerator claveAccesoGenerator,
      XmlComprobanteGenerator xmlGenerator,
      SriXmlValidator xmlValidator,
      DigitalSignatureService signatureService,
      OfflineModePolicy offlineModePolicy,
      RetryPolicy retryPolicy,
      SriClient sriClient,
      TimeProvider timeProvider
  ) {
    this.repository = repository;
    this.eventStore = eventStore;
    this.auditLogRepository = auditLogRepository;
    this.validator = validator;
    this.stateMachine = stateMachine;
    this.claveAccesoGenerator = claveAccesoGenerator;
    this.xmlGenerator = xmlGenerator;
    this.xmlValidator = xmlValidator;
    this.signatureService = signatureService;
    this.offlineModePolicy = offlineModePolicy;
    this.retryPolicy = retryPolicy;
    this.sriClient = sriClient;
    this.timeProvider = timeProvider;
  }

  @Override
  public ComprobanteId emitir(EmitirComprobanteCommand command) {
    Instant ahora = timeProvider.now();
    Comprobante comprobante = Comprobante.crear(
        ComprobanteId.newId(),
        command.tipo(),
        command.infoTributaria(),
        command.infoDocumento(),
        command.detalles(),
        ahora
    );

    ClaveAcceso claveAcceso = claveAccesoGenerator.generar(
        comprobante.infoTributaria(),
        comprobante.tipo(),
        command.codigoNumerico(),
        comprobante.infoDocumento() instanceof ec.sri.einvoice.domain.model.InfoFactura infoFactura
            ? infoFactura.fechaEmision()
            : ahora.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    );
    comprobante.asignarClaveAcceso(claveAcceso);
    RuntimeException error = null;
    try {
      validator.validateOrThrow(comprobante);
      stateMachine.transition(comprobante, EstadoComprobante.VALIDADO, ahora, "Validacion exitosa");

      String xml = xmlGenerator.generar(comprobante);
      xmlValidator.validar(comprobante.tipo(), xml);
      comprobante.registrarXml(xml);

      String xmlFirmado = signatureService.firmar(xml, comprobante.infoTributaria());
      comprobante.registrarXmlFirmado(xmlFirmado);
      stateMachine.transition(comprobante, EstadoComprobante.FIRMADO, ahora, "Firmado");

      if (offlineModePolicy.isOfflineEnabled()) {
        comprobante.programarSiguienteReintento(retryPolicy.nextAttemptTime(0, ahora));
        stateMachine.transition(comprobante, EstadoComprobante.EN_COLA, ahora, "En cola offline");
      } else {
        procesarEnvio(comprobante, ahora);
      }
    } catch (RuntimeException ex) {
      error = ex;
      stateMachine.transition(comprobante, EstadoComprobante.ERROR, ahora, "Error al emitir: " + ex.getMessage());
    } finally {
      repository.save(comprobante);
      eventStore.append(comprobante.pullEvents());
      String accion = (error == null) ? "EMITIR" : "EMITIR_ERROR";
      String detalle = (error == null)
          ? "Comprobante creado"
          : "Error al emitir: " + (error.getMessage() == null ? "sin detalle" : error.getMessage());
      auditLogRepository.save(new BitacoraEntry(UUID.randomUUID(), comprobante.id(), accion, detalle, ahora));
    }

    if (error != null) {
      throw error;
    }
    return comprobante.id();
  }

      private void procesarEnvio(Comprobante comprobante, Instant ahora) {
        stateMachine.transition(comprobante, EstadoComprobante.ENVIADO, ahora, "Enviado a SRI");
        SriResponse response = sriClient.enviar(comprobante, comprobante.xmlFirmado());
    if (response.status() == SriResponseStatus.AUTORIZADO) {
      comprobante.registrarAutorizacion(response.numeroAutorizacion());
      stateMachine.transition(comprobante, EstadoComprobante.AUTORIZADO, ahora, "Autorizado por SRI");
      return;
    }
    if (response.status() == SriResponseStatus.NO_AUTORIZADO) {
      comprobante.registrarRechazo(response.mensaje());
      stateMachine.transition(comprobante, EstadoComprobante.RECHAZADO, ahora, "Rechazado por SRI");
      return;
    }
    if (response.status() == SriResponseStatus.ENVIADO_SRI || response.status() == SriResponseStatus.EN_PROCESO) {
      return;
    }
    comprobante.incrementarIntento(ahora, retryPolicy.nextAttemptTime(comprobante.intentosEnvio(), ahora));
    stateMachine.transition(comprobante, EstadoComprobante.ERROR, ahora, "Error de envio");
  }
}
