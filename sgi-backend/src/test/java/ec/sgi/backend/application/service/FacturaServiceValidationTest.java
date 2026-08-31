package ec.sgi.backend.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ec.sgi.backend.application.exception.ConfiguracionTributariaIncompletaException;
import ec.sgi.backend.application.port.in.CrearFacturaCommand;
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
import ec.sgi.backend.domain.model.Empresa;
import ec.sgi.backend.domain.model.FirmaElectronica;
import ec.sgi.backend.domain.model.RegimenTributario;
import ec.sgi.backend.domain.service.FacturaTotalsCalculator;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
        List.of(),
        List.of()
    );

    assertThatThrownBy(() -> service.crear(command))
        .isInstanceOf(ConfiguracionTributariaIncompletaException.class)
        .hasMessageContaining("Configuracion tributaria incompleta")
        .hasMessageContaining("direccion matriz");
    verify(sriCorePort, never()).emitirFactura(org.mockito.ArgumentMatchers.any());
  }
}
