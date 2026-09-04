package ec.sgi.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ec.sgi.backend.application.dto.SriEmitirFacturaRequest;
import ec.sgi.backend.application.dto.SriEmitirFacturaResult;
import ec.sgi.backend.application.dto.SriEnvioStatus;
import ec.sgi.backend.application.exception.ConfiguracionTributariaIncompletaException;
import ec.sgi.backend.application.port.in.CrearFacturaCommand;
import ec.sgi.backend.application.port.in.ItemFacturaCommand;
import ec.sgi.backend.application.port.in.PagoFacturaCommand;
import ec.sgi.backend.application.port.out.BodegaRepository;
import ec.sgi.backend.application.port.out.ClienteRepository;
import ec.sgi.backend.application.port.out.CuentaPorCobrarRepository;
import ec.sgi.backend.application.port.out.DocumentoClienteRepository;
import ec.sgi.backend.application.port.out.EmpresaRepository;
import ec.sgi.backend.application.port.out.FacturaRepository;
import ec.sgi.backend.application.port.out.FirmaElectronicaRepository;
import ec.sgi.backend.application.port.out.ImpuestoRepository;
import ec.sgi.backend.application.port.out.InventarioRepository;
import ec.sgi.backend.application.port.out.PreordenRepository;
import ec.sgi.backend.application.port.out.ProductoRepository;
import ec.sgi.backend.application.port.out.SriCorePort;
import ec.sgi.backend.application.usecase.EnviarFacturaPorEmailUseCase;
import ec.sgi.backend.domain.model.Cliente;
import ec.sgi.backend.domain.model.Empresa;
import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.FacturaImpuestoTotal;
import ec.sgi.backend.domain.model.FacturaItem;
import ec.sgi.backend.domain.model.FacturaTotales;
import ec.sgi.backend.domain.model.FirmaElectronica;
import ec.sgi.backend.domain.model.Impuesto;
import ec.sgi.backend.domain.model.Producto;
import ec.sgi.backend.domain.model.RegimenTributario;
import ec.sgi.backend.domain.service.FacturaTotalsCalculator;
import ec.sgi.backend.domain.service.FacturaCalculoResult;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FacturaServiceValidationTest {
  @Mock private FacturaRepository facturaRepository;
  @Mock private ClienteRepository clienteRepository;
  @Mock private DocumentoClienteRepository documentoClienteRepository;
  @Mock private CuentaPorCobrarRepository cuentaPorCobrarRepository;
  @Mock private ProductoRepository productoRepository;
  @Mock private ImpuestoRepository impuestoRepository;
  @Mock private InventarioRepository inventarioRepository;
  @Mock private PreordenRepository preordenRepository;
  @Mock private EmpresaRepository empresaRepository;
  @Mock private FirmaElectronicaRepository firmaElectronicaRepository;
  @Mock private BodegaRepository bodegaRepository;
  @Mock private SriCorePort sriCorePort;
  @Mock private FacturaTotalsCalculator totalsCalculator;
  @Mock private EnviarFacturaPorEmailUseCase enviarFacturaPorEmailUseCase;

  private FacturaService service;
  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    service = new FacturaService(
        facturaRepository,
        clienteRepository,
        documentoClienteRepository,
        cuentaPorCobrarRepository,
        productoRepository,
        impuestoRepository,
        inventarioRepository,
        preordenRepository,
        empresaRepository,
        firmaElectronicaRepository,
        bodegaRepository,
        sriCorePort,
        totalsCalculator,
        enviarFacturaPorEmailUseCase,
        10,
        false,
        true,
        true,
        true,
        false,
        0
    );
  }

  @Test
  void configuracionTributariaIncompletaNoEnviaFacturaAlSri() {
    Empresa empresa = new Empresa(
        1L,
        "PRODUCCION",
        "NORMAL",
        "EMPRESA TEST",
        "EMPRESA TEST",
        "1790012345001",
        "",
        "001",
        "001",
        "000000001",
        null,
        false,
        RegimenTributario.RIMPE_EMPRENDEDOR,
        false,
        null,
        false,
        30
    );
    FirmaElectronica firma = new FirmaElectronica(
        1L,
        1L,
        "firma.p12",
        "application/x-pkcs12",
        "/tmp/firma.p12",
        "secret"
    );
    when(empresaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(empresa));
    when(firmaElectronicaRepository.findByEmpresaId(1L)).thenReturn(Optional.of(firma));

    CrearFacturaCommand command = new CrearFacturaCommand(
        1L,
        2L,
        null,
        "Sucursal",
        LocalDate.of(2026, 8, 27),
        "USD",
        "12345678",
        null,
        List.of(),
        List.of()
    );

    assertThatThrownBy(() -> service.crear(command))
        .isInstanceOf(ConfiguracionTributariaIncompletaException.class)
        .hasMessageContaining("Configuracion tributaria incompleta")
        .hasMessageContaining("direccion matriz");
    verify(sriCorePort, never()).emitirFactura(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void ambientePruebasUsaSecuencialPruebasSinIncrementarProduccion() throws Exception {
    Path firmaPath = Files.createFile(tempDir.resolve("firma.p12"));
    Empresa empresa = new Empresa(
        1L,
        "PRUEBAS",
        "NORMAL",
        "EMPRESA TEST",
        "EMPRESA TEST",
        "1790012345001",
        "Direccion matriz",
        "001",
        "001",
        "000000100",
        "000000006",
        null,
        false,
        RegimenTributario.RIMPE_EMPRENDEDOR,
        false,
        null,
        false,
        30
    );
    Cliente cliente = new Cliente(
        2L,
        1L,
        "05",
        "0912345678",
        "CLIENTE",
        "cliente@example.com",
        "Direccion cliente",
        null
    );
    Producto producto = new Producto(
        5L,
        1L,
        "SKU-01",
        "Producto",
        new BigDecimal("10.00"),
        1L,
        7L,
        null,
        true,
        "SKU-01"
    );
    Impuesto impuesto = new Impuesto(
        7L,
        1L,
        "2",
        "4",
        new BigDecimal("15.00"),
        "IVA 15",
        true
    );
    FacturaItem facturaItem = new FacturaItem(
        null,
        5L,
        "SKU-01",
        "Producto",
        BigDecimal.ONE,
        new BigDecimal("10.00"),
        BigDecimal.ZERO,
        new BigDecimal("10.00"),
        List.of()
    );
    FacturaCalculoResult calculo = new FacturaCalculoResult(
        List.of(facturaItem),
        new FacturaTotales(
            new BigDecimal("10.00"),
            BigDecimal.ZERO,
            new BigDecimal("1.50"),
            new BigDecimal("11.50")
        ),
        List.of(new FacturaImpuestoTotal("2", "4", new BigDecimal("10.00"), new BigDecimal("1.50")))
    );
    FirmaElectronica firma = new FirmaElectronica(
        1L,
        1L,
        "firma.p12",
        "application/x-pkcs12",
        firmaPath.toString(),
        "secret"
    );
    when(empresaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(empresa));
    when(firmaElectronicaRepository.findByEmpresaId(1L)).thenReturn(Optional.of(firma));
    when(clienteRepository.findByIdAndEmpresaId(2L, 1L)).thenReturn(Optional.of(cliente));
    when(productoRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(producto));
    when(impuestoRepository.findByIdAndEmpresaId(7L, 1L)).thenReturn(Optional.of(impuesto));
    when(totalsCalculator.calcular(any())).thenReturn(calculo);
    when(facturaRepository.save(any())).thenAnswer(invocation -> {
      Factura factura = invocation.getArgument(0);
      return factura;
    });
    when(sriCorePort.emitirFactura(any())).thenReturn(new SriEmitirFacturaResult(
        "core-1",
        "clave",
        SriEnvioStatus.RECIBIDO,
        "RECIBIDA",
        null,
        "<factura/>"
    ));

    service.crear(new CrearFacturaCommand(
        1L,
        2L,
        null,
        "Sucursal",
        LocalDate.of(2026, 9, 3),
        "USD",
        "12345678",
        null,
        List.of(new ItemFacturaCommand(null, 5L, BigDecimal.ONE, BigDecimal.ZERO)),
        List.of(new PagoFacturaCommand("EFECTIVO", new BigDecimal("11.50")))
    ));

    ArgumentCaptor<SriEmitirFacturaRequest> sriRequestCaptor =
        ArgumentCaptor.forClass(SriEmitirFacturaRequest.class);
    verify(sriCorePort).emitirFactura(sriRequestCaptor.capture());
    assertThat(sriRequestCaptor.getValue().infoTributaria().secuencial()).isEqualTo("000000006");

    ArgumentCaptor<Empresa> empresaCaptor = ArgumentCaptor.forClass(Empresa.class);
    verify(empresaRepository).save(empresaCaptor.capture());
    assertThat(empresaCaptor.getValue().secuencial()).isEqualTo("000000100");
    assertThat(empresaCaptor.getValue().secuencialPruebas()).isEqualTo("000000007");
    verify(inventarioRepository, never()).findByProductoIdAndEmpresaId(any(), any());
    verify(inventarioRepository, never()).save(any());
  }
}
