package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.EmpresaResult;
import java.util.List;

public interface ListarEmpresasUseCase {
  List<EmpresaResult> listar();
}
