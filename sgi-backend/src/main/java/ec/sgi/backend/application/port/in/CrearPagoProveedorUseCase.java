package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.PagoProveedorResult;

public interface CrearPagoProveedorUseCase {
  PagoProveedorResult crear(CrearPagoProveedorCommand command);
}
