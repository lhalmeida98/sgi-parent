package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.DocumentoClienteEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoClienteJpaRepository extends JpaRepository<DocumentoClienteEntity, Long> {
  Optional<DocumentoClienteEntity> findByIdAndEmpresaId(Long id, Long empresaId);

  Optional<DocumentoClienteEntity> findByFacturaId(Long facturaId);

  List<DocumentoClienteEntity> findByEmpresaId(Long empresaId);

  List<DocumentoClienteEntity> findByClienteId(Long clienteId);

  List<DocumentoClienteEntity> findByIdInAndEmpresaId(List<Long> ids, Long empresaId);

  boolean existsByEmpresaIdAndNumeroFactura(Long empresaId, String numeroFactura);
}
