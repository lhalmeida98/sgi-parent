package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Impuesto;
import java.util.List;
import java.util.Optional;

public interface ImpuestoRepository {
  Impuesto save(Impuesto impuesto);

  Optional<Impuesto> findById(Long id);

  List<Impuesto> findAll();

  List<Impuesto> findByEmpresaId(Long empresaId);

  Optional<Impuesto> findByIdAndEmpresaId(Long id, Long empresaId);
}
