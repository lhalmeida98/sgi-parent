package ec.sri.einvoice.domain.exception;

import ec.sri.einvoice.domain.model.EstadoComprobante;

public class InvalidStateException extends DomainException {
  public InvalidStateException(EstadoComprobante from, EstadoComprobante to) {
    super("INVALID_STATE", "Transicion no permitida: " + from + " -> " + to);
  }
}
