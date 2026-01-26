package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ImpuestoResult;

public interface ActualizarImpuestoUseCase {
  ImpuestoResult actualizar(Long empresaId, Long impuestoId, ActualizarImpuestoCommand command);
}
