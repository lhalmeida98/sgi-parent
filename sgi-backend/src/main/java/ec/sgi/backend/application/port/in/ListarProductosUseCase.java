package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ProductoResult;
import java.util.List;

public interface ListarProductosUseCase {
  List<ProductoResult> listar(Long empresaId);
}
