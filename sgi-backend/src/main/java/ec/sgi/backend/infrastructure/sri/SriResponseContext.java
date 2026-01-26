package ec.sgi.backend.infrastructure.sri;

import ec.sri.einvoice.application.port.out.SriResponse;
import org.springframework.stereotype.Component;

@Component
public class SriResponseContext {
  private final ThreadLocal<SriResponse> holder = new ThreadLocal<>();

  public void set(SriResponse response) {
    if (response == null) {
      holder.remove();
      return;
    }
    holder.set(response);
  }

  public SriResponse consume() {
    SriResponse response = holder.get();
    holder.remove();
    return response;
  }

  public void clear() {
    holder.remove();
  }
}
