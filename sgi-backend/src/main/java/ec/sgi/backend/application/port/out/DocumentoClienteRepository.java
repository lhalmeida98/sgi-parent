package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.DocumentoCliente;
import java.util.List;
import java.util.Optional;

public interface DocumentoClienteRepository {
  DocumentoCliente save(DocumentoCliente documento);

  Optional<DocumentoCliente> findById(Long id);

  Optional<DocumentoCliente> findByIdAndEmpresaId(Long id, Long empresaId);

  Optional<DocumentoCliente> findByFacturaId(Long facturaId);

  List<DocumentoCliente> findByIdInAndEmpresaId(List<Long> ids, Long empresaId);

  List<DocumentoCliente> findByEmpresaId(Long empresaId);

  List<DocumentoCliente> findByClienteId(Long clienteId);

  boolean existsByEmpresaIdAndNumeroFactura(Long empresaId, String numeroFactura);
}
