package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.AccionResult;
import java.util.List;

public interface ListarAccionesUseCase {
  List<AccionResult> listar();
}
