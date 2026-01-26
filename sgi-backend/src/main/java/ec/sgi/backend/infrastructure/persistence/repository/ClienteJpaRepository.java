package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.ClienteEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Long> {
  List<ClienteEntity> findByEmpresaId(Long empresaId);

  Optional<ClienteEntity> findByIdAndEmpresaId(Long id, Long empresaId);
}
