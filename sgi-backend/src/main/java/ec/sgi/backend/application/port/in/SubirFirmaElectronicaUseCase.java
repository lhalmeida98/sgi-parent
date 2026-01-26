package ec.sgi.backend.application.port.in;

import ec.sgi.backend.application.dto.FirmaElectronicaResult;

public interface SubirFirmaElectronicaUseCase {
  FirmaElectronicaResult subir(SubirFirmaElectronicaCommand command);
}
