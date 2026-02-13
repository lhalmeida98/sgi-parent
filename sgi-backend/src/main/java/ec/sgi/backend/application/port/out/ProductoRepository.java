package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
  Producto save(Producto producto);

  List<Producto> findAll();

  Optional<Producto> findById(Long id);

  List<Producto> findByEmpresaId(Long empresaId);

  Optional<Producto> findByIdAndEmpresaId(Long id, Long empresaId);

  Optional<Producto> findByEmpresaIdAndCodigo(Long empresaId, String codigo);

  Optional<Producto> findByEmpresaIdAndCodigoBarras(Long empresaId, String codigoBarras);
}
