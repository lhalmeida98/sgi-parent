package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.CobroClienteEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CobroClienteJpaRepository extends JpaRepository<CobroClienteEntity, Long> {
  Optional<CobroClienteEntity> findByIdAndEmpresaId(Long id, Long empresaId);

  List<CobroClienteEntity> findByEmpresaId(Long empresaId);

  List<CobroClienteEntity> findByClienteId(Long clienteId);
}
