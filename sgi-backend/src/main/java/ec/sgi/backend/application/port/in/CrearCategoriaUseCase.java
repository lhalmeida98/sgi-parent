package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.CategoriaCreateResult;

public interface CrearCategoriaUseCase {
  CategoriaCreateResult crear(CrearCategoriaCommand command);
}
