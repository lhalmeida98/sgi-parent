package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.ClienteSriConsultaResult;
import ec.sgi.backend.application.dto.SriContribuyenteInfo;
import ec.sgi.backend.application.port.in.ConsultarClienteSriUseCase;
import ec.sgi.backend.application.port.out.SriContribuyentePort;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ClienteSriConsultaService implements ConsultarClienteSriUseCase {
  private final SriContribuyentePort sriContribuyentePort;

  public ClienteSriConsultaService(SriContribuyentePort sriContribuyentePort) {
    this.sriContribuyentePort = sriContribuyentePort;
  }

  @Override
  public ClienteSriConsultaResult consultar(String identificacion) {
    if (identificacion == null || identificacion.isBlank()) {
      return new ClienteSriConsultaResult(false, "Identificacion requerida", null);
    }
    try {
      Optional<SriContribuyenteInfo> info = sriContribuyentePort.consultarPorRuc(identificacion.trim());
      if (info.isEmpty()) {
        return new ClienteSriConsultaResult(false, "No se encontraron datos en SRI. Llena los datos manualmente.", null);
      }
      return new ClienteSriConsultaResult(true, null, info.get());
    } catch (RuntimeException ex) {
      return new ClienteSriConsultaResult(false, "No se pudo consultar SRI. Llena los datos manualmente.", null);
    }
  }
}
