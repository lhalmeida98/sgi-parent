package ec.sgi.backend.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import ec.sgi.backend.domain.service.FacturaCalculoResult;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class FacturaTotalsCalculatorTest {
  @Test
  void calculaTotalesConImpuesto() {
    FacturaTotalsCalculator calculator = new FacturaTotalsCalculator();
    ItemCalculo item = new ItemCalculo(
        null,
        1L,
        "P-001",
        "Producto A",
        new BigDecimal("2"),
        new BigDecimal("10.00"),
        new BigDecimal("1.00"),
        "2",
        "2",
        new BigDecimal("12.00")
    );

    FacturaCalculoResult result = calculator.calcular(List.of(item));

    assertThat(result.totales().totalSinImpuestos()).isEqualByComparingTo("19.00");
    assertThat(result.totales().totalDescuento()).isEqualByComparingTo("1.00");
    assertThat(result.totales().totalImpuestos()).isEqualByComparingTo("2.28");
    assertThat(result.totales().importeTotal()).isEqualByComparingTo("21.28");
    assertThat(result.impuestosTotales()).hasSize(1);
  }

  @Test
  void calculaIvaSinSubirCentavoCuandoElTercerDecimalRedondeariaArriba() {
    FacturaTotalsCalculator calculator = new FacturaTotalsCalculator();
    ItemCalculo item = new ItemCalculo(
        null,
        1L,
        "P-001",
        "Producto A",
        new BigDecimal("1"),
        new BigDecimal("9.57"),
        new BigDecimal("0.00"),
        "2",
        "4",
        new BigDecimal("15.00")
    );

    FacturaCalculoResult result = calculator.calcular(List.of(item));

    assertThat(result.totales().totalSinImpuestos()).isEqualByComparingTo("9.57");
    assertThat(result.totales().totalImpuestos()).isEqualByComparingTo("1.43");
    assertThat(result.totales().importeTotal()).isEqualByComparingTo("11.00");
  }
}
