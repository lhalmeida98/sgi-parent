package ec.sgi.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import ec.sgi.backend.domain.model.Cliente;
import ec.sgi.backend.domain.model.Empresa;
import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.FacturaEstado;
import ec.sgi.backend.domain.model.FacturaItem;
import ec.sgi.backend.domain.model.FacturaTotales;
import ec.sgi.backend.domain.model.InfoTributariaData;
import ec.sgi.backend.domain.model.RegimenTributario;
import ec.sgi.backend.domain.model.SriEstado;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class FacturaPdfServiceTest {
  private final FacturaPdfService service = new FacturaPdfService(null, null, null);

  @Test
  void rideMuestraRimpeEmprendedorYObligadoContabilidadNo() {
    Factura factura = factura(
        info("EMPRESA A", "COMERCIAL A", "1790012345001", false, RegimenTributario.RIMPE_EMPRENDEDOR),
        LocalDate.of(2026, 8, 27),
        LocalDateTime.of(2026, 8, 27, 17, 9, 3)
    );

    String html = service.buildHtml(factura, empresaViva("EMPRESA EDITADA", "1799999999001"), cliente());

    assertThat(html).contains("CONTRIBUYENTE RÉGIMEN RIMPE");
    assertThat(html).contains("OBLIGADO A LLEVAR CONTABILIDAD:</strong> NO");
  }

  @Test
  void rideGeneralNoMuestraRimpeYObligadoContabilidadSi() {
    Factura factura = factura(
        info("EMPRESA GENERAL", "GENERAL", "1790012345001", true, RegimenTributario.GENERAL),
        LocalDate.of(2026, 8, 27),
        LocalDateTime.of(2026, 8, 27, 17, 9, 3)
    );

    String html = service.buildHtml(factura, empresaViva("EMPRESA GENERAL", "1790012345001"), cliente());

    assertThat(html).doesNotContain("CONTRIBUYENTE RÉGIMEN RIMPE");
    assertThat(html).contains("OBLIGADO A LLEVAR CONTABILIDAD:</strong> SI");
  }

  @Test
  void rideUsaSnapshotDeFacturaYNoDatosVivosDeOtraEmpresa() {
    Factura factura = factura(
        info("EMPRESA A", "COMERCIAL A", "1790012345001", false, RegimenTributario.GENERAL),
        LocalDate.of(2026, 8, 27),
        LocalDateTime.of(2026, 8, 27, 17, 9, 3)
    );

    String html = service.buildHtml(factura, empresaViva("EMPRESA B", "1799999999001"), cliente());

    assertThat(html).contains("EMPRESA A");
    assertThat(html).contains("1790012345001");
    assertThat(html).doesNotContain("EMPRESA B");
    assertThat(html).doesNotContain("1799999999001");
  }

  @Test
  void rideDistingueFechaEmisionDeFechaAutorizacion() {
    Factura factura = factura(
        info("EMPRESA A", "COMERCIAL A", "1790012345001", false, RegimenTributario.GENERAL),
        LocalDate.of(2026, 8, 27),
        LocalDateTime.of(2026, 8, 28, 10, 11, 12)
    );

    String html = service.buildHtml(factura, empresaViva("EMPRESA A", "1790012345001"), cliente());

    assertThat(html).contains("FECHA DE EMISION:</strong> 27/08/2026");
    assertThat(html).contains("FECHA Y HORA DE AUTORIZACION:</strong> 28/08/2026 10:11:12");
    assertThat(html).doesNotContain("27/08/2026 00:00:00");
  }

  private Factura factura(InfoTributariaData info, LocalDate fechaEmision, LocalDateTime fechaAutorizacion) {
    return new Factura(
        1L,
        10L,
        20L,
        null,
        info,
        fechaEmision,
        "Sucursal",
        "USD",
        List.of(new FacturaItem(
            null,
            1L,
            "SKU-01",
            "Producto",
            BigDecimal.ONE,
            new BigDecimal("10.00"),
            BigDecimal.ZERO,
            new BigDecimal("10.00"),
            List.of()
        )),
        new FacturaTotales(
            new BigDecimal("10.00"),
            BigDecimal.ZERO,
            new BigDecimal("1.50"),
            new BigDecimal("11.50")
        ),
        List.of(),
        FacturaEstado.AUTORIZADA,
        "2708202601179001234500110010010000000011234567811",
        null,
        new SriEstado("CONSULTADA", "AUTORIZADO", null),
        "2708202601179001234500110010010000000011234567811",
        fechaAutorizacion,
        null,
        null,
        0,
        null
    );
  }

  private InfoTributariaData info(
      String razonSocial,
      String nombreComercial,
      String ruc,
      boolean obligadoContabilidad,
      RegimenTributario regimen
  ) {
    return new InfoTributariaData(
        "2",
        "1",
        razonSocial,
        nombreComercial,
        ruc,
        "Direccion matriz",
        "001",
        "001",
        "000000001",
        obligadoContabilidad,
        regimen,
        false,
        null,
        false
    );
  }

  private Empresa empresaViva(String razonSocial, String ruc) {
    return new Empresa(
        99L,
        "2",
        "1",
        razonSocial,
        razonSocial,
        ruc,
        "Otra direccion",
        "001",
        "001",
        "000000001",
        null,
        true,
        RegimenTributario.RIMPE_EMPRENDEDOR,
        false,
        null,
        false,
        30
    );
  }

  private Cliente cliente() {
    return new Cliente(
        20L,
        10L,
        "05",
        "0912345678",
        "CLIENTE",
        "cliente@example.com",
        "Direccion cliente",
        null
    );
  }
}
