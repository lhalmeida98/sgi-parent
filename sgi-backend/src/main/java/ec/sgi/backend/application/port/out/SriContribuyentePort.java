package ec.sgi.backend.application.port.out;

import ec.sgi.backend.application.dto.SriContribuyenteInfo;
import java.util.Optional;

public interface SriContribuyentePort {
  Optional<SriContribuyenteInfo> consultarPorRuc(String ruc);
}
