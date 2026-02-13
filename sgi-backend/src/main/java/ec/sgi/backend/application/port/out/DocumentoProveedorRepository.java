package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.DocumentoProveedor;
import java.util.List;
import java.util.Optional;

public interface DocumentoProveedorRepository {
  DocumentoProveedor save(DocumentoProveedor documento);

  Optional<DocumentoProveedor> findById(Long id);

  Optional<DocumentoProveedor> findByIdAndEmpresaId(Long id, Long empresaId);

  List<DocumentoProveedor> findByIdInAndEmpresaId(List<Long> ids, Long empresaId);

  List<DocumentoProveedor> findByEmpresaId(Long empresaId);

  List<DocumentoProveedor> findByProveedorId(Long proveedorId);

  boolean existsByEmpresaIdAndProveedorIdAndNumeroDocumento(Long empresaId, Long proveedorId, String numeroDocumento);

  Optional<DocumentoProveedor> findByEmpresaIdAndNumeroAutorizacion(Long empresaId, String numeroAutorizacion);
}
