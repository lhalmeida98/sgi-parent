package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.UsuarioRolEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRolJpaRepository extends JpaRepository<UsuarioRolEntity, Long> {
  List<UsuarioRolEntity> findByUsuarioId(Long usuarioId);

  List<UsuarioRolEntity> findByUsuarioIdIn(Collection<Long> usuarioIds);

  void deleteByUsuarioId(Long usuarioId);

  @Query("select r.nombre from RolEntity r join UsuarioRolEntity ur on ur.rolId = r.id "
      + "where ur.usuarioId = :usuarioId and (r.activo = true or r.activo is null)")
  List<String> findRoleNamesByUsuarioId(@Param("usuarioId") Long usuarioId);
}
