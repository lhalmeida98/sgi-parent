package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Proveedor;
import java.util.List;
import java.util.Optional;

public interface ProveedorRepository {
  Proveedor save(Proveedor proveedor);

  Optional<Proveedor> findById(Long id);

  Optional<Proveedor> findByIdAndEmpresaId(Long id, Long empresaId);

  Optional<Proveedor> findByEmpresaIdAndIdentificacion(Long empresaId, String identificacion);

  List<Proveedor> findByEmpresaId(Long empresaId);

  boolean existsByEmpresaIdAndIdentificacion(Long empresaId, String identificacion);
}
