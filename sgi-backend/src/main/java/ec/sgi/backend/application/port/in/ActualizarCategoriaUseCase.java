package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.CategoriaResult;

public interface ActualizarCategoriaUseCase {
  CategoriaResult actualizar(Long empresaId, Long categoriaId, ActualizarCategoriaCommand command);
}
