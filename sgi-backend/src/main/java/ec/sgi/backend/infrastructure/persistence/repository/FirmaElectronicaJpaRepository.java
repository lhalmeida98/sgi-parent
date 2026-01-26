package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.FirmaElectronicaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FirmaElectronicaJpaRepository extends JpaRepository<FirmaElectronicaEntity, Long> {
  @Query("select f from FirmaElectronicaEntity f where f.empresa.id = :empresaId")
  Optional<FirmaElectronicaEntity> findByEmpresaId(@Param("empresaId") Long empresaId);
}
