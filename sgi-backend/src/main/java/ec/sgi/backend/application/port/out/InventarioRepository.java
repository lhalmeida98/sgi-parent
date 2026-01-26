package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Inventario;
import java.util.List;
import java.util.Optional;

public interface InventarioRepository {
  Optional<Inventario> findByProductoIdForUpdate(Long productoId);

  Optional<Inventario> findByProductoIdAndEmpresaIdForUpdate(Long productoId, Long empresaId);

  Inventario save(Inventario inventario);

  List<Inventario> findAll();

  List<Inventario> findByEmpresaId(Long empresaId);
}
