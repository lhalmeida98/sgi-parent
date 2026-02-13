package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ProductoResult;

public interface BuscarProductoPorCodigoUseCase {
  ProductoResult buscar(Long empresaId, String codigo);
}
