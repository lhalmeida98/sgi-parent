package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.InventarioDetalleResult;

public interface ConsultarInventarioProductoBodegaUseCase {
  InventarioDetalleResult consultar(Long empresaId, Long productoId, Long bodegaId);
}
