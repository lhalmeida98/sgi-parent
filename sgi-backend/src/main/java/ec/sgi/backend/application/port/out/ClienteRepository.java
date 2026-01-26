package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository {
  Cliente save(Cliente cliente);

  List<Cliente> findAll();

  Optional<Cliente> findById(Long id);

  List<Cliente> findByEmpresaId(Long empresaId);

  Optional<Cliente> findByIdAndEmpresaId(Long id, Long empresaId);
}
