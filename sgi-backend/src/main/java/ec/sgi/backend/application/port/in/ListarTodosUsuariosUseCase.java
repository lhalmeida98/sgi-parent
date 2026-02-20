package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.UsuarioResult;
import java.util.List;

public interface ListarTodosUsuariosUseCase {
  List<UsuarioResult> listarTodos();
}
