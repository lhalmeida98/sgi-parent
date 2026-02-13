package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ProductoResult;

public interface ActualizarProductoVendibleUseCase {
  ProductoResult actualizarVendible(Long empresaId, Long productoId, boolean vendible);
}
