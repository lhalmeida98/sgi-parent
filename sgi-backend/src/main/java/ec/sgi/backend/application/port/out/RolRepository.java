package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Rol;
import java.util.List;
import java.util.Optional;

public interface RolRepository {
  Rol save(Rol rol);

  List<Rol> findAll();

  Optional<Rol> findById(Long id);

  Optional<Rol> findByNombre(String nombreRol);

  List<String> findPermisosByRoles(List<String> nombresRoles);

  boolean existsPermiso(String nombreRol, String permiso);

  boolean existsByNombre(String nombreRol);

  void deleteById(Long id);
}
