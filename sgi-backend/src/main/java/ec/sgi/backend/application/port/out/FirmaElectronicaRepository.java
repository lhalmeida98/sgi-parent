package ec.sgi.backend.application.port.out;

import ec.sgi.backend.domain.model.FirmaElectronica;
import java.util.Optional;

public interface FirmaElectronicaRepository {
  FirmaElectronica save(FirmaElectronica firma);

  Optional<FirmaElectronica> findByEmpresaId(Long empresaId);
}
