package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.ImpuestoCreateResult;

public interface CrearImpuestoUseCase {
  ImpuestoCreateResult crear(CrearImpuestoCommand command);
}
