package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.ProveedorSriConsultaResult;
import ec.sgi.backend.application.dto.SriContribuyenteInfo;
import ec.sgi.backend.application.port.in.ConsultarProveedorSriUseCase;
import ec.sgi.backend.application.port.out.SriContribuyentePort;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProveedorSriConsultaService implements ConsultarProveedorSriUseCase {
  private final SriContribuyentePort sriContribuyentePort;

  public ProveedorSriConsultaService(SriContribuyentePort sriContribuyentePort) {
    this.sriContribuyentePort = sriContribuyentePort;
  }

  @Override
  public ProveedorSriConsultaResult consultar(String identificacion) {
    if (identificacion == null || identificacion.isBlank()) {
      return new ProveedorSriConsultaResult(false, "Identificacion requerida", null);
    }
    try {
      Optional<SriContribuyenteInfo> info = sriContribuyentePort.consultarPorRuc(identificacion.trim());
      if (info.isEmpty()) {
        return new ProveedorSriConsultaResult(false, "No existe en SRI", null);
      }
      return new ProveedorSriConsultaResult(true, null, info.get());
    } catch (RuntimeException ex) {
      return new ProveedorSriConsultaResult(false, ex.getMessage(), null);
    }
  }
}
