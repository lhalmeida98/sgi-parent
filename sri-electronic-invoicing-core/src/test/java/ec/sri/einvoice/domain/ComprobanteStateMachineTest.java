package ec.sri.einvoice.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.sri.einvoice.domain.exception.InvalidStateException;
import ec.sri.einvoice.domain.model.EstadoComprobante;
import ec.sri.einvoice.domain.service.ComprobanteStateMachine;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ComprobanteStateMachineTest {
  @Test
  void permiteTransicionValida() {
    var comprobante = ComprobanteTestData.facturaValida();
    var stateMachine = new ComprobanteStateMachine();

    stateMachine.transition(comprobante, EstadoComprobante.VALIDADO, Instant.now(), "Validado");
  }

  @Test
  void rechazaTransicionInvalida() {
    var comprobante = ComprobanteTestData.facturaValida();
    var stateMachine = new ComprobanteStateMachine();

    assertThatThrownBy(() -> stateMachine.transition(comprobante, EstadoComprobante.AUTORIZADO, Instant.now(), "No valido"))
        .isInstanceOf(InvalidStateException.class);
  }
}
