package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.UsuarioEmpresaDetalleResult;
import java.util.List;

public interface ListarEmpresasUsuarioUseCase {
  List<UsuarioEmpresaDetalleResult> listarEmpresas(Long empresaId, Long usuarioId);
}
