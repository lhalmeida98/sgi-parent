package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.CobroCliente;
import java.util.List;
import java.util.Optional;

public interface CobroClienteRepository {
  CobroCliente save(CobroCliente cobro);

  Optional<CobroCliente> findById(Long id);

  Optional<CobroCliente> findByIdAndEmpresaId(Long id, Long empresaId);

  List<CobroCliente> findByEmpresaId(Long empresaId);

  List<CobroCliente> findByClienteId(Long clienteId);
}
