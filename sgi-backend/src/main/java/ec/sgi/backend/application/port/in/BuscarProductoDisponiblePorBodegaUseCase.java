package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.InventarioProductoDisponibleResult;

public interface BuscarProductoDisponiblePorBodegaUseCase {
  InventarioProductoDisponibleResult buscar(Long empresaId, Long bodegaId, String codigo);
}
