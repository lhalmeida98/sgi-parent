package ec.sgi.backend.infrastructure.sri;

import ec.sri.einvoice.application.port.out.SriResponse;
import org.springframework.stereotype.Component;

@Component
public class SriResponseContext {
  private final ThreadLocal<SriResponseSnapshot> holder = new ThreadLocal<>();

  public void set(SriResponse response) {
    set(response, null);
  }

  public void set(SriResponse response, String xmlFirmado) {
    if (response == null && (xmlFirmado == null || xmlFirmado.isBlank())) {
      holder.remove();
      return;
    }
    holder.set(new SriResponseSnapshot(response, xmlFirmado));
  }

  public SriResponse consume() {
    SriResponseSnapshot snapshot = holder.get();
    holder.remove();
    return snapshot == null ? null : snapshot.response();
  }

  public SriResponseSnapshot consumeSnapshot() {
    SriResponseSnapshot snapshot = holder.get();
    holder.remove();
    return snapshot;
  }

  public void clear() {
    holder.remove();
  }
}
