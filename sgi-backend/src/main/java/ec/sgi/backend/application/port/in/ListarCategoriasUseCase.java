package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.CategoriaResult;
import java.util.List;

public interface ListarCategoriasUseCase {
  List<CategoriaResult> listar(Long empresaId);
}
