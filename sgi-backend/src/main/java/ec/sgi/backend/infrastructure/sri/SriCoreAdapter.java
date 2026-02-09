package ec.sgi.backend.infrastructure.sri;

import ec.sgi.backend.application.dto.SriConsultaEstadoRequest;
import ec.sgi.backend.application.dto.SriConsultaEstadoResult;
import ec.sgi.backend.application.dto.SriEmitirFacturaRequest;
import ec.sgi.backend.application.dto.SriEmitirFacturaResult;
import ec.sgi.backend.application.dto.SriEnvioStatus;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.SriCoreException;
import ec.sgi.backend.application.port.out.SriCorePort;
import ec.sri.einvoice.application.port.in.ConsultarComprobanteUseCase;
import ec.sri.einvoice.application.port.in.EmitirComprobanteUseCase;
import ec.sri.einvoice.application.port.out.SriConsultaAutorizacionResponse;
import ec.sri.einvoice.application.port.out.SriResponse;
import ec.sri.einvoice.application.port.out.SriResponseStatus;
import ec.sri.einvoice.domain.model.ClaveAcceso;
import ec.sri.einvoice.domain.model.ComprobanteId;
import ec.sri.einvoice.domain.service.ClaveAccesoGenerator;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import ec.sgi.backend.infrastructure.sri.SriResponseSnapshot;

@Component
public class SriCoreAdapter implements SriCorePort {
  private final EmitirComprobanteUseCase emitirComprobanteUseCase;
  private final ConsultarComprobanteUseCase consultarComprobanteUseCase;
  private final ClaveAccesoGenerator claveAccesoGenerator;
  private final SriResponseContext responseContext;

  public SriCoreAdapter(
      EmitirComprobanteUseCase emitirComprobanteUseCase,
      ConsultarComprobanteUseCase consultarComprobanteUseCase,
      ClaveAccesoGenerator claveAccesoGenerator,
      SriResponseContext responseContext
  ) {
    this.emitirComprobanteUseCase = emitirComprobanteUseCase;
    this.consultarComprobanteUseCase = consultarComprobanteUseCase;
    this.claveAccesoGenerator = claveAccesoGenerator;
    this.responseContext = responseContext;
  }

  @Override
  public SriEmitirFacturaResult emitirFactura(SriEmitirFacturaRequest request) {
    try {
      responseContext.clear();
      var command = SriCoreMapper.toEmitirCommand(request);
      ComprobanteId comprobanteId = emitirComprobanteUseCase.emitir(command);
      LocalDate fechaEmision = request.infoFactura().fechaEmision();
      ClaveAcceso claveAcceso = claveAccesoGenerator.generar(
          command.infoTributaria(),
          command.tipo(),
          request.codigoNumerico(),
          fechaEmision
      );
      SriResponseSnapshot snapshot = responseContext.consumeSnapshot();
      SriResponse response = snapshot == null ? null : snapshot.response();
      String xmlFirmado = snapshot == null ? null : snapshot.xmlFirmado();
      SriEnvioStatus estadoSri = mapStatus(response);
      String mensajeSri = response == null ? null : response.mensaje();
      String numeroAutorizacion = response == null ? null : response.numeroAutorizacion();
      return new SriEmitirFacturaResult(
          comprobanteId.value().toString(),
          claveAcceso.value(),
          estadoSri,
          mensajeSri,
          numeroAutorizacion,
          xmlFirmado
      );
    } catch (BusinessRuleException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new SriCoreException("Error al emitir comprobante en core SRI", ex);
    }
  }

  @Override
  public SriConsultaEstadoResult consultarEstado(SriConsultaEstadoRequest request) {
    try {
      SriConsultaAutorizacionResponse response = consultarComprobanteUseCase.consultar(
          SriCoreMapper.toConsultarCommand(request)
      );
      return new SriConsultaEstadoResult(
          response.estadoConsulta(),
          response.estadoAutorizacion(),
          response.mensaje(),
          response.claveAcceso(),
          response.comprobanteXml()
      );
    } catch (BusinessRuleException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new SriCoreException("Error al consultar estado en core SRI", ex);
    }
  }

  private SriEnvioStatus mapStatus(SriResponse response) {
    if (response == null) {
      return null;
    }
    return switch (response.status()) {
      case GENERADO, ENVIADO_SRI -> SriEnvioStatus.RECIBIDO;
      case EN_PROCESO -> SriEnvioStatus.EN_PROCESO;
      case AUTORIZADO -> SriEnvioStatus.AUTORIZADO;
      case NO_AUTORIZADO -> SriEnvioStatus.RECHAZADO;
      case ERROR -> SriEnvioStatus.ERROR;
    };
  }

}
