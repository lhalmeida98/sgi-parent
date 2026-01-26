package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Bodega;
import java.util.List;

public interface BodegaRepository {
  Bodega save(Bodega bodega);

  List<Bodega> findByEmpresaId(Long empresaId);
}
