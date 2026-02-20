package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Accion;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccionRepository {
  Accion save(Accion accion);

  List<Accion> findAll();

  Optional<Accion> findById(Long id);

  Optional<Accion> findByCodigo(String codigo);

  List<Accion> findByCodigoIn(Collection<String> codigos);

  boolean existsByCodigo(String codigo);

  boolean existsActiveByCodigo(String codigo);

  void deleteById(Long id);
}
