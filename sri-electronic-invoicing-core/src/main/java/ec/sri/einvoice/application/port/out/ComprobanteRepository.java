package ec.sri.einvoice.application.port.out;

import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.domain.model.ComprobanteId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ComprobanteRepository {
  Comprobante save(Comprobante comprobante);
  Optional<Comprobante> findById(ComprobanteId id);
  List<Comprobante> findPendientes(Instant ahora);
}
