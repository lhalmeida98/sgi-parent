package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.DocumentoProveedorEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoProveedorJpaRepository extends JpaRepository<DocumentoProveedorEntity, Long> {
  List<DocumentoProveedorEntity> findByEmpresaId(Long empresaId);

  List<DocumentoProveedorEntity> findByProveedorId(Long proveedorId);

  Optional<DocumentoProveedorEntity> findByIdAndEmpresaId(Long id, Long empresaId);

  List<DocumentoProveedorEntity> findByIdInAndEmpresaId(List<Long> ids, Long empresaId);

  boolean existsByEmpresaIdAndProveedorIdAndNumeroDocumento(Long empresaId, Long proveedorId, String numeroDocumento);

  Optional<DocumentoProveedorEntity> findByEmpresaIdAndNumeroAutorizacion(Long empresaId, String numeroAutorizacion);
}
