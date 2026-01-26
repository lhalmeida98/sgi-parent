package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Rol;
import java.util.List;

public interface RolRepository {
  Rol save(Rol rol);

  List<Rol> findByEmpresaId(Long empresaId);

  boolean existsPermiso(Long empresaId, String nombreRol, String permiso);

  boolean existsByNombre(Long empresaId, String nombreRol);
}
