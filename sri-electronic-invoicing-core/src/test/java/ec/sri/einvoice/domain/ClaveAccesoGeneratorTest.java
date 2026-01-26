package ec.sri.einvoice.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.sri.einvoice.domain.model.Ambiente;
import ec.sri.einvoice.domain.model.ClaveAcceso;
import ec.sri.einvoice.domain.model.InfoTributaria;
import ec.sri.einvoice.domain.model.TipoComprobante;
import ec.sri.einvoice.domain.model.TipoEmision;
import ec.sri.einvoice.domain.service.ClaveAccesoGenerator;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ClaveAccesoGeneratorTest {
  @Test
  void generaClaveAccesoConFormatoCorrecto() {
    InfoTributaria infoTributaria = new InfoTributaria(
        Ambiente.PRUEBAS,
        TipoEmision.NORMAL,
        "Empresa Demo",
        "Empresa Demo",
        "1790012345001",
        "Quito",
        "001",
        "002",
        "000000123",
        null,
        null,
        null
    );
    ClaveAccesoGenerator generator = new ClaveAccesoGenerator();

    String clave = generator.generar(infoTributaria, TipoComprobante.FACTURA, "12345678", LocalDate.of(2024, 1, 15)).value();

    assertThat(clave).hasSize(49);
    assertThat(clave).startsWith("15012024" + "01" + "1790012345001" + "1" + "001002" + "000000123" + "12345678" + "1");
  }

  @Test
  void fallaCuandoCodigoNumericoSuperaLongitud() {
    InfoTributaria infoTributaria = new InfoTributaria(
        Ambiente.PRUEBAS,
        TipoEmision.NORMAL,
        "Empresa Demo",
        "Empresa Demo",
        "1790012345001",
        "Quito",
        "001",
        "002",
        "000000123",
        null,
        null,
        null
    );
    ClaveAccesoGenerator generator = new ClaveAccesoGenerator();

    assertThatThrownBy(() -> generator.generar(infoTributaria, TipoComprobante.FACTURA, "123456789", LocalDate.of(2024, 1, 15)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void fallaCuandoDigitoVerificadorNoCoincide() {
    InfoTributaria infoTributaria = new InfoTributaria(
        Ambiente.PRUEBAS,
        TipoEmision.NORMAL,
        "Empresa Demo",
        "Empresa Demo",
        "1790012345001",
        "Quito",
        "001",
        "002",
        "000000123",
        null,
        null,
        null
    );
    ClaveAccesoGenerator generator = new ClaveAccesoGenerator();

    String clave = generator.generar(infoTributaria, TipoComprobante.FACTURA, "12345678", LocalDate.of(2024, 1, 15)).value();
    char ultimo = clave.charAt(clave.length() - 1);
    char reemplazo = ultimo == '0' ? '1' : '0';
    String invalida = clave.substring(0, 48) + reemplazo;

    assertThatThrownBy(() -> ClaveAcceso.of(invalida))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
