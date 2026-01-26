package ec.sri.einvoice.application.port.out;

import ec.sri.einvoice.domain.model.BitacoraEntry;

public interface AuditLogRepository {
  void save(BitacoraEntry entry);
}
