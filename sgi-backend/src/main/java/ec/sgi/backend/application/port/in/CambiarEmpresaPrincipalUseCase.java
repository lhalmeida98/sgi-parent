package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.UsuarioResult;

public interface CambiarEmpresaPrincipalUseCase {
  UsuarioResult cambiar(Long empresaId, Long usuarioId, Long empresaPrincipalId);
}
