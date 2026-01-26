package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Preorden;
import java.util.List;
import java.util.Optional;

public interface PreordenRepository {
  Preorden save(Preorden preorden);

  Optional<Preorden> findById(Long id);

  Optional<Preorden> findByIdAndEmpresaId(Long id, Long empresaId);

  List<Preorden> findAll();

  List<Preorden> findByEmpresaId(Long empresaId);
}
