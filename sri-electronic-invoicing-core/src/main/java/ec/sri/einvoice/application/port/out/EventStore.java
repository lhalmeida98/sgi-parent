package ec.sri.einvoice.application.port.out;

import ec.sri.einvoice.domain.event.ComprobanteEvent;
import java.util.List;

public interface EventStore {
  void append(List<ComprobanteEvent> events);
}
