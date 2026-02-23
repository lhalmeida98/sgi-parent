package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Inventario;
import java.util.List;
import java.util.Optional;

public interface InventarioRepository {
  Optional<Inventario> findByProductoIdForUpdate(Long productoId);

  Optional<Inventario> findByProductoIdAndEmpresaIdForUpdate(Long productoId, Long empresaId);

  Optional<Inventario> findByProductoIdAndEmpresaIdAndBodegaIdForUpdate(Long productoId, Long empresaId, Long bodegaId);

  List<Inventario> findByProductoIdAndEmpresaId(Long productoId, Long empresaId);

  Optional<Inventario> findByProductoIdAndEmpresaIdAndBodegaId(Long productoId, Long empresaId, Long bodegaId);

  List<Inventario> findByEmpresaIdAndBodegaId(Long empresaId, Long bodegaId);

  Inventario save(Inventario inventario);

  List<Inventario> findAll();

  List<Inventario> findByEmpresaId(Long empresaId);

  long countStockCriticoByEmpresaId(Long empresaId);
}
