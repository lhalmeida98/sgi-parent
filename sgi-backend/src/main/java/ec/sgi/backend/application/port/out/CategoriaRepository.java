package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Categoria;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository {
  Categoria save(Categoria categoria);

  List<Categoria> findAll();

  Optional<Categoria> findById(Long id);

  List<Categoria> findByEmpresaId(Long empresaId);

  Optional<Categoria> findByIdAndEmpresaId(Long id, Long empresaId);
}
