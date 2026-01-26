package ec.sri.einvoice.infrastructure.persistence.adapter;

import ec.sri.einvoice.application.port.out.AuditLogRepository;
import ec.sri.einvoice.domain.model.BitacoraEntry;
import ec.sri.einvoice.infrastructure.persistence.entity.BitacoraEntity;
import ec.sri.einvoice.infrastructure.persistence.repository.JpaBitacoraRepository;
import org.springframework.stereotype.Component;

@Component
public class AuditLogAdapter implements AuditLogRepository {
  private final JpaBitacoraRepository repository;

  public AuditLogAdapter(JpaBitacoraRepository repository) {
    this.repository = repository;
  }

  @Override
  public void save(BitacoraEntry entry) {
    BitacoraEntity entity = new BitacoraEntity();
    entity.setId(entry.id());
    entity.setComprobanteId(entry.comprobanteId().value());
    entity.setAccion(entry.accion());
    entity.setDetalle(entry.detalle());
    entity.setOcurridoEn(entry.ocurridoEn());
    repository.save(entity);
  }
}
