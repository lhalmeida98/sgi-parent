package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
  Usuario save(Usuario usuario);

  List<Usuario> findByEmpresaId(Long empresaId);

  Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

  Optional<Usuario> findByEmail(String email);

  Optional<Usuario> findByUsuario(String usuario);
}
