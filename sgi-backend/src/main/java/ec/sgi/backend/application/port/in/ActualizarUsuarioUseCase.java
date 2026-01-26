package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.UsuarioResult;

public interface ActualizarUsuarioUseCase {
  UsuarioResult actualizar(Long empresaId, Long usuarioId, ActualizarUsuarioCommand command);
}
