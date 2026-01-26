package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Empresa;
import java.util.List;
import java.util.Optional;

public interface EmpresaRepository {
  Empresa save(Empresa empresa);

  Optional<Empresa> findById(Long id);

  Optional<Empresa> findByIdForUpdate(Long id);

  List<Empresa> findAll();
}
