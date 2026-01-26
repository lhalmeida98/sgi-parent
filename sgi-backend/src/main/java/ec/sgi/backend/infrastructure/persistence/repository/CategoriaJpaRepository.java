package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.CategoriaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaJpaRepository extends JpaRepository<CategoriaEntity, Long> {
  List<CategoriaEntity> findByEmpresaId(Long empresaId);

  Optional<CategoriaEntity> findByIdAndEmpresaId(Long id, Long empresaId);
}
