package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.InventarioProductoDisponibleResult;

public interface BuscarProductoDisponiblePorIdUseCase {
  InventarioProductoDisponibleResult buscar(Long empresaId, Long bodegaId, Long productoId);
}
