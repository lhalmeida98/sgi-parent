package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.FacturaEstado;
import java.util.List;
import java.util.Optional;

public interface FacturaRepository {
  Factura save(Factura factura);

  Optional<Factura> findById(Long id);

  List<Factura> findByEstado(FacturaEstado estado);

  List<Factura> findByEstadoAndEmpresaId(FacturaEstado estado, Long empresaId);
}
