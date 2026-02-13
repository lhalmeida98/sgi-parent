package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Bodega;
import java.util.List;
import java.util.Optional;

public interface BodegaRepository {
  Bodega save(Bodega bodega);

  List<Bodega> findByEmpresaId(Long empresaId);

  Optional<Bodega> findByIdAndEmpresaId(Long id, Long empresaId);
}
