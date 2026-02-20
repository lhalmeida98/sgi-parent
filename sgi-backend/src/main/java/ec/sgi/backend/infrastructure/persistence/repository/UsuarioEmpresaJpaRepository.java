package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.domain.model.UsuarioEmpresa;
import ec.sgi.backend.infrastructure.persistence.entity.UsuarioEmpresaEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioEmpresaJpaRepository extends JpaRepository<UsuarioEmpresaEntity, Long> {
  List<UsuarioEmpresaEntity> findByUsuarioIdIn(Collection<Long> usuarioIds);

  void deleteByUsuarioId(Long usuarioId);

  boolean existsByUsuarioIdAndEmpresaId(Long usuarioId, Long empresaId);

  @Query("select ue.usuarioId from UsuarioEmpresaEntity ue where ue.empresaId = :empresaId")
  List<Long> findUsuarioIdsByEmpresaId(@Param("empresaId") Long empresaId);

  @Query("select new ec.sgi.backend.domain.model.UsuarioEmpresa(ue.empresaId, "
      + "case when ue.principal = true then true else false end) "
      + "from UsuarioEmpresaEntity ue where ue.usuarioId = :usuarioId")
  List<UsuarioEmpresa> findEmpresasByUsuarioId(@Param("usuarioId") Long usuarioId);
}
