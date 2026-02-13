package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.PagoProveedor;
import java.util.List;
import java.util.Optional;

public interface PagoProveedorRepository {
  PagoProveedor save(PagoProveedor pago);

  Optional<PagoProveedor> findById(Long id);

  Optional<PagoProveedor> findByIdAndEmpresaId(Long id, Long empresaId);

  List<PagoProveedor> findByEmpresaId(Long empresaId);

  List<PagoProveedor> findByProveedorId(Long proveedorId);
}
