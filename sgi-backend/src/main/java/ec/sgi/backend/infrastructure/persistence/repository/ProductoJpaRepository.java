package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.ProductoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {
  List<ProductoEntity> findByEmpresaId(Long empresaId);

  Optional<ProductoEntity> findByIdAndEmpresaId(Long id, Long empresaId);

  Optional<ProductoEntity> findByEmpresaIdAndCodigo(Long empresaId, String codigo);

  Optional<ProductoEntity> findByEmpresaIdAndCodigoBarras(Long empresaId, String codigoBarras);
}
