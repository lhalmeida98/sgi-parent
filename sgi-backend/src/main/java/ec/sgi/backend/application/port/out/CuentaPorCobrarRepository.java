package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.CuentaPorCobrar;
import java.util.List;
import java.util.Optional;

public interface CuentaPorCobrarRepository {
  CuentaPorCobrar save(CuentaPorCobrar cuenta);

  Optional<CuentaPorCobrar> findById(Long id);

  Optional<CuentaPorCobrar> findByIdAndEmpresaId(Long id, Long empresaId);

  Optional<CuentaPorCobrar> findByDocumentoClienteId(Long documentoClienteId);

  List<CuentaPorCobrar> findByEmpresaId(Long empresaId);

  List<CuentaPorCobrar> findByClienteId(Long clienteId);

  List<CuentaPorCobrar> findByClienteIdAndEmpresaId(Long clienteId, Long empresaId);
}
