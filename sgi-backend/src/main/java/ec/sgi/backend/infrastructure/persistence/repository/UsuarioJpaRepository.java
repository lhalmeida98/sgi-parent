package ec.sgi.backend.infrastructure.persistence.repository;

import ec.sgi.backend.infrastructure.persistence.entity.UsuarioEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {
  List<UsuarioEntity> findByEmpresaId(Long empresaId);

  Optional<UsuarioEntity> findByIdAndEmpresaId(Long id, Long empresaId);

  Optional<UsuarioEntity> findByEmail(String email);

  Optional<UsuarioEntity> findByUsuario(String usuario);

  Optional<UsuarioEntity> findByEmailOrUsuario(String email, String usuario);
}
