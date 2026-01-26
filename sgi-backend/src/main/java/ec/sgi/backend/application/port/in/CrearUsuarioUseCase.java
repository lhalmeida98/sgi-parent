package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.UsuarioCreateResult;

public interface CrearUsuarioUseCase {
  UsuarioCreateResult crear(CrearUsuarioCommand command);
}
