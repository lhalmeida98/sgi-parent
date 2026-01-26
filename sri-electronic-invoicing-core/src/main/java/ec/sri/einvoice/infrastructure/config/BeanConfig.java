package ec.sri.einvoice.infrastructure.config;

import ec.sri.einvoice.application.port.in.ConsultarAutorizacionLoteUseCase;
import ec.sri.einvoice.application.port.in.ConsultarComprobanteUseCase;
import ec.sri.einvoice.application.port.in.ConsultarFacturaComercialUseCase;
import ec.sri.einvoice.application.port.in.EmitirComprobanteUseCase;
import ec.sri.einvoice.application.port.in.EnviarLoteUseCase;
import ec.sri.einvoice.application.port.in.ReintentarEnvioUseCase;
import ec.sri.einvoice.application.port.out.AuditLogRepository;
import ec.sri.einvoice.application.port.out.ComprobanteRepository;
import ec.sri.einvoice.application.port.out.DigitalSignatureService;
import ec.sri.einvoice.application.port.out.EventStore;
import ec.sri.einvoice.application.port.out.OfflineModePolicy;
import ec.sri.einvoice.application.port.out.RetryPolicy;
import ec.sri.einvoice.application.port.out.SriClient;
import ec.sri.einvoice.application.port.out.SriConsultaClient;
import ec.sri.einvoice.application.port.out.SriLoteClient;
import ec.sri.einvoice.application.port.out.SriXmlValidator;
import ec.sri.einvoice.application.port.out.TimeProvider;
import ec.sri.einvoice.application.port.out.XmlComprobanteGenerator;
import ec.sri.einvoice.application.service.ConsultarAutorizacionLoteService;
import ec.sri.einvoice.application.service.ConsultarComprobanteService;
import ec.sri.einvoice.application.service.ConsultarFacturaComercialService;
import ec.sri.einvoice.application.service.EmitirComprobanteService;
import ec.sri.einvoice.application.service.EnviarLoteService;
import ec.sri.einvoice.application.service.ReintentarEnvioService;
import ec.sri.einvoice.domain.service.AlphanumericWhitespaceRule;
import ec.sri.einvoice.domain.service.ClaveAccesoGenerator;
import ec.sri.einvoice.domain.service.ComprobanteStateMachine;
import ec.sri.einvoice.domain.service.ComprobanteValidator;
import ec.sri.einvoice.domain.service.DetalleRule;
import ec.sri.einvoice.domain.service.RucLengthRule;
import ec.sri.einvoice.domain.service.TotalFacturaRule;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class BeanConfig {
  @Bean
  public ComprobanteValidator comprobanteValidator() {
    return new ComprobanteValidator(List.of(
        new RucLengthRule(),
        new AlphanumericWhitespaceRule(),
        new DetalleRule(),
        new TotalFacturaRule()
    ));
  }

  @Bean
  public ComprobanteStateMachine comprobanteStateMachine() {
    return new ComprobanteStateMachine();
  }

  @Bean
  public ClaveAccesoGenerator claveAccesoGenerator() {
    return new ClaveAccesoGenerator();
  }

  @Bean
  public EmitirComprobanteUseCase emitirComprobanteUseCase(
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
    return new EmitirComprobanteService(
        repository,
        eventStore,
        auditLogRepository,
        validator,
        stateMachine,
        claveAccesoGenerator,
        xmlGenerator,
        xmlValidator,
        signatureService,
        offlineModePolicy,
        retryPolicy,
        sriClient,
        timeProvider
    );
  }

  @Bean
  public ReintentarEnvioUseCase reintentarEnvioUseCase(
      ComprobanteRepository repository,
      EventStore eventStore,
      AuditLogRepository auditLogRepository,
      SriClient sriClient,
      RetryPolicy retryPolicy,
      ComprobanteStateMachine stateMachine,
      TimeProvider timeProvider
  ) {
    return new ReintentarEnvioService(
        repository,
        eventStore,
        auditLogRepository,
        sriClient,
        retryPolicy,
        stateMachine,
        timeProvider
    );
  }

  @Bean
  public ConsultarComprobanteUseCase consultarComprobanteUseCase(SriConsultaClient sriConsultaClient) {
    return new ConsultarComprobanteService(sriConsultaClient);
  }

  @Bean
  public ConsultarFacturaComercialUseCase consultarFacturaComercialUseCase(SriConsultaClient sriConsultaClient) {
    return new ConsultarFacturaComercialService(sriConsultaClient);
  }

  @Bean
  public EnviarLoteUseCase enviarLoteUseCase(SriLoteClient sriLoteClient) {
    return new EnviarLoteService(sriLoteClient);
  }

  @Bean
  public ConsultarAutorizacionLoteUseCase consultarAutorizacionLoteUseCase(SriLoteClient sriLoteClient) {
    return new ConsultarAutorizacionLoteService(sriLoteClient);
  }
}
