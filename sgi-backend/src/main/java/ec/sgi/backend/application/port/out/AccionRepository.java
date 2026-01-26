package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Accion;
import java.util.List;

public interface AccionRepository {
  Accion save(Accion accion);

  List<Accion> findByEmpresaId(Long empresaId);

  boolean existsByCodigo(Long empresaId, String codigo);

  boolean existsActiveByCodigo(Long empresaId, String codigo);
}
