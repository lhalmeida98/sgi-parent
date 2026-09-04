package ec.sgi.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import ec.sgi.backend.application.dto.SriEmitirFacturaRequest;
import ec.sgi.backend.application.dto.SriInfoTributariaDto;
import ec.sgi.backend.application.port.in.CrearFacturaCommand;
import ec.sgi.backend.application.port.in.ItemFacturaCommand;
import ec.sgi.backend.application.port.in.PagoFacturaCommand;
import ec.sgi.backend.domain.model.Cliente;
import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.FacturaEstado;
import ec.sgi.backend.domain.model.FacturaImpuestoTotal;
import ec.sgi.backend.domain.model.FacturaItem;
import ec.sgi.backend.domain.model.FacturaTotales;
import ec.sgi.backend.domain.model.InfoTributariaData;
import ec.sgi.backend.domain.model.RegimenTributario;
import ec.sgi.backend.domain.model.SriEstado;
import ec.sgi.backend.domain.service.FacturaCalculoResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class SriEmitirFacturaRequestBuilderTest {
  @Test
  void generaPagoSriCreditoConPlazoYFechaVencimiento() {
    CrearFacturaCommand command = new CrearFacturaCommand(
        1L,
        2L,
        null,
        "Sucursal",
        LocalDate.of(2026, 9, 2),
        "USD",
        "12345678",
        null,
        List.of(new ItemFacturaCommand(1L, 5L, BigDecimal.ONE, BigDecimal.ZERO)),
        List.of(new PagoFacturaCommand("CREDITO", new BigDecimal("112.00")))
    );
    Cliente cliente = new Cliente(
        2L,
        1L,
        "05",
        "0912345678",
        "CLIENTE CREDITO",
        "cliente@example.com",
        "Direccion cliente",
        30
    );
    FacturaItem item = new FacturaItem(
        null,
        5L,
        "SKU-01",
        "Producto",
        BigDecimal.ONE,
        new BigDecimal("100.00"),
        BigDecimal.ZERO,
        new BigDecimal("100.00"),
        List.of()
    );
    FacturaCalculoResult calculo = new FacturaCalculoResult(
        List.of(item),
        new FacturaTotales(
            new BigDecimal("100.00"),
            BigDecimal.ZERO,
            new BigDecimal("12.00"),
            new BigDecimal("112.00")
        ),
        List.of(new FacturaImpuestoTotal("2", "4", new BigDecimal("100.00"), new BigDecimal("12.00")))
    );
    InfoTributariaData info = new InfoTributariaData(
        "2",
        "1",
        "EMPRESA",
        "EMPRESA",
        "1790012345001",
        "Matriz",
        "001",
        "001",
        "000000001",
        false,
        RegimenTributario.GENERAL,
        false,
        null,
        false
    );
    Factura factura = new Factura(
        10L,
        1L,
        2L,
        null,
        info,
        LocalDate.of(2026, 9, 2),
        "Sucursal",
        "USD",
        null,
        List.of(item),
        calculo.totales(),
        List.of(),
        FacturaEstado.CREADA,
        null,
        null,
        new SriEstado("PENDIENTE", null, null),
        null,
        null,
        null,
        null,
        0,
        null
    );
    SriInfoTributariaDto infoTributaria = new SriInfoTributariaDto(
        "2",
        "1",
        "EMPRESA",
        "EMPRESA",
        "1790012345001",
        "Matriz",
        "001",
        "001",
        "000000001",
        "NO",
        null,
        null,
        null,
        "firma.p12",
        "secret"
    );

    SriEmitirFacturaRequest request = SriEmitirFacturaRequestBuilder.build(
        command,
        cliente,
        calculo,
        factura,
        BigDecimal.ZERO,
        infoTributaria
    );

    assertThat(request.pagos()).hasSize(1);
    assertThat(request.pagos().get(0).formaPago()).isEqualTo("20");
    assertThat(request.pagos().get(0).plazo()).isEqualTo(30);
    assertThat(request.pagos().get(0).unidadTiempo()).isEqualTo("dias");
    assertThat(request.infoAdicional())
        .anySatisfy(campo -> {
          assertThat(campo.nombre()).isEqualTo("Forma de pago");
          assertThat(campo.valor()).isEqualTo("CREDITO 30 dias");
        })
        .anySatisfy(campo -> {
          assertThat(campo.nombre()).isEqualTo("Factura vence");
          assertThat(campo.valor()).isEqualTo("02/10/2026");
        });
  }
}
