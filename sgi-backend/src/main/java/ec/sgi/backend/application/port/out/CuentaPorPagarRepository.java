package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.CuentaPorPagar;
import java.util.List;
import java.util.Optional;

public interface CuentaPorPagarRepository {
  CuentaPorPagar save(CuentaPorPagar cuenta);

  Optional<CuentaPorPagar> findById(Long id);

  Optional<CuentaPorPagar> findByIdAndEmpresaId(Long id, Long empresaId);

  Optional<CuentaPorPagar> findByDocumentoProveedorId(Long documentoProveedorId);

  List<CuentaPorPagar> findByEmpresaId(Long empresaId);

  List<CuentaPorPagar> findByProveedorId(Long proveedorId);

  List<CuentaPorPagar> findByProveedorIdAndEmpresaId(Long proveedorId, Long empresaId);
}
