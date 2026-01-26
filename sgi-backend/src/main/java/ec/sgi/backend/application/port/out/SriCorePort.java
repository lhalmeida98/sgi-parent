package ec.sgi.backend.application.port.out;

import ec.sgi.backend.application.dto.SriConsultaEstadoRequest;
import ec.sgi.backend.application.dto.SriConsultaEstadoResult;
import ec.sgi.backend.application.dto.SriEmitirFacturaRequest;
import ec.sgi.backend.application.dto.SriEmitirFacturaResult;

public interface SriCorePort {
  SriEmitirFacturaResult emitirFactura(SriEmitirFacturaRequest request);

  SriConsultaEstadoResult consultarEstado(SriConsultaEstadoRequest request);
}
