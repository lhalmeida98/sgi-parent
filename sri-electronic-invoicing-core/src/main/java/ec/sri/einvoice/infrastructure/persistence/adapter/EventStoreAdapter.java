package ec.sri.einvoice.infrastructure.persistence.adapter;

import ec.sri.einvoice.application.port.out.EventStore;
import ec.sri.einvoice.domain.event.ComprobanteEvent;
import ec.sri.einvoice.infrastructure.persistence.entity.ComprobanteEventEntity;
import ec.sri.einvoice.infrastructure.persistence.repository.JpaComprobanteEventRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EventStoreAdapter implements EventStore {
  private final JpaComprobanteEventRepository repository;

  public EventStoreAdapter(JpaComprobanteEventRepository repository) {
    this.repository = repository;
  }

  @Override
  public void append(List<ComprobanteEvent> events) {
    List<ComprobanteEventEntity> entities = events.stream().map(this::toEntity).toList();
    repository.saveAll(entities);
  }

  private ComprobanteEventEntity toEntity(ComprobanteEvent event) {
    ComprobanteEventEntity entity = new ComprobanteEventEntity();
    entity.setId(event.id());
    entity.setComprobanteId(event.comprobanteId().value());
    entity.setTipo(event.tipo().name());
    entity.setDetalle(event.detalle());
    entity.setOcurridoEn(event.ocurridoEn());
    return entity;
  }
}
