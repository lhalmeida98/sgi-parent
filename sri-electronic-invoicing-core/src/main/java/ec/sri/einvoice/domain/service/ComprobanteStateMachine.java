package ec.sri.einvoice.domain.service;

import ec.sri.einvoice.domain.exception.InvalidStateException;
import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.domain.model.EstadoComprobante;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class ComprobanteStateMachine {
  private static final Map<EstadoComprobante, Set<EstadoComprobante>> TRANSICIONES = new EnumMap<>(EstadoComprobante.class);

  static {
    TRANSICIONES.put(EstadoComprobante.CREADO, EnumSet.of(EstadoComprobante.VALIDADO, EstadoComprobante.ERROR));
    TRANSICIONES.put(EstadoComprobante.VALIDADO, EnumSet.of(EstadoComprobante.FIRMADO, EstadoComprobante.ERROR));
    TRANSICIONES.put(EstadoComprobante.FIRMADO, EnumSet.of(EstadoComprobante.EN_COLA, EstadoComprobante.ENVIADO, EstadoComprobante.ERROR));
    TRANSICIONES.put(EstadoComprobante.EN_COLA, EnumSet.of(EstadoComprobante.ENVIADO, EstadoComprobante.ERROR));
    TRANSICIONES.put(EstadoComprobante.ENVIADO, EnumSet.of(EstadoComprobante.AUTORIZADO, EstadoComprobante.RECHAZADO, EstadoComprobante.ERROR));
    TRANSICIONES.put(EstadoComprobante.AUTORIZADO, EnumSet.noneOf(EstadoComprobante.class));
    TRANSICIONES.put(EstadoComprobante.RECHAZADO, EnumSet.noneOf(EstadoComprobante.class));
    TRANSICIONES.put(EstadoComprobante.ERROR, EnumSet.of(EstadoComprobante.EN_COLA));
  }

  public void transition(Comprobante comprobante, EstadoComprobante nuevoEstado, Instant ahora, String detalle) {
    EstadoComprobante actual = comprobante.estado();
    if (!TRANSICIONES.getOrDefault(actual, EnumSet.noneOf(EstadoComprobante.class)).contains(nuevoEstado)) {
      throw new InvalidStateException(actual, nuevoEstado);
    }
    comprobante.aplicarEstado(nuevoEstado, ahora, detalle);
  }
}
