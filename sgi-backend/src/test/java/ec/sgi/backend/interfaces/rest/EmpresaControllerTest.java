package ec.sgi.backend.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ec.sgi.backend.application.dto.EmpresaResult;
import ec.sgi.backend.application.dto.EmpresaUpdateRequest;
import ec.sgi.backend.application.dto.FirmaElectronicaResult;
import ec.sgi.backend.application.exception.ForbiddenException;
import ec.sgi.backend.application.port.in.ActualizarEmpresaUseCase;
import ec.sgi.backend.application.port.in.CrearEmpresaUseCase;
import ec.sgi.backend.application.port.in.ListarEmpresasUsuarioUseCase;
import ec.sgi.backend.application.port.in.SubirFirmaElectronicaUseCase;
import ec.sgi.backend.application.port.in.SubirLogoEmpresaUseCase;
import ec.sgi.backend.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class EmpresaControllerTest {
  @Mock
  private CrearEmpresaUseCase crearEmpresaUseCase;
  @Mock
  private ListarEmpresasUsuarioUseCase listarEmpresasUsuarioUseCase;
  @Mock
  private SubirFirmaElectronicaUseCase subirFirmaElectronicaUseCase;
  @Mock
  private ActualizarEmpresaUseCase actualizarEmpresaUseCase;
  @Mock
  private SubirLogoEmpresaUseCase subirLogoEmpresaUseCase;
  @Mock
  private CurrentUserService currentUserService;

  private EmpresaController controller;

  @BeforeEach
  void setUp() {
    controller = new EmpresaController(
        crearEmpresaUseCase,
        listarEmpresasUsuarioUseCase,
        subirFirmaElectronicaUseCase,
        actualizarEmpresaUseCase,
        subirLogoEmpresaUseCase,
        currentUserService
    );
  }

  @Test
  void semiAdminPuedeActualizarSuEmpresaActual() {
    when(currentUserService.isAdmin()).thenReturn(false);
    when(currentUserService.getEmpresaId()).thenReturn(2L);
    when(actualizarEmpresaUseCase.actualizar(any(), any())).thenReturn(empresaResult(2L));

    controller.actualizar(2L, updateRequest());

    verify(actualizarEmpresaUseCase).actualizar(any(), any());
  }

  @Test
  void semiAdminNoPuedeActualizarOtraEmpresa() {
    when(currentUserService.isAdmin()).thenReturn(false);
    when(currentUserService.getEmpresaId()).thenReturn(2L);

    assertThatThrownBy(() -> controller.actualizar(1L, updateRequest()))
        .isInstanceOf(ForbiddenException.class);

    verify(actualizarEmpresaUseCase, never()).actualizar(any(), any());
  }

  @Test
  void semiAdminPuedeSubirFirmaDeSuEmpresaActual() {
    MockMultipartFile archivo = new MockMultipartFile(
        "archivo",
        "firma.p12",
        "application/x-pkcs12",
        new byte[] {1, 2, 3}
    );
    when(currentUserService.isAdmin()).thenReturn(false);
    when(currentUserService.getEmpresaId()).thenReturn(2L);
    when(subirFirmaElectronicaUseCase.subir(any())).thenReturn(
        new FirmaElectronicaResult(5L, "firma.p12", "application/x-pkcs12")
    );

    controller.subirFirma(2L, archivo, "clave");

    verify(subirFirmaElectronicaUseCase).subir(any());
  }

  @Test
  void semiAdminNoPuedeSubirFirmaDeOtraEmpresa() {
    MockMultipartFile archivo = new MockMultipartFile(
        "archivo",
        "firma.p12",
        "application/x-pkcs12",
        new byte[] {1, 2, 3}
    );
    when(currentUserService.isAdmin()).thenReturn(false);
    when(currentUserService.getEmpresaId()).thenReturn(2L);

    assertThatThrownBy(() -> controller.subirFirma(1L, archivo, "clave"))
        .isInstanceOf(ForbiddenException.class);

    verify(subirFirmaElectronicaUseCase, never()).subir(any());
  }

  private EmpresaUpdateRequest updateRequest() {
    return new EmpresaUpdateRequest(
        "PRUEBAS",
        "NORMAL",
        "LUIS HENRY ALMEIDA FLORES",
        "MyM mundo repuestos",
        "JIPIJAPA AV. EL INCA E14-38 Y N47B DE LOS NOGALES",
        "001",
        "001",
        "000000001",
        false,
        false,
        30
    );
  }

  private EmpresaResult empresaResult(Long id) {
    return new EmpresaResult(
        id,
        "PRUEBAS",
        "NORMAL",
        "LUIS HENRY ALMEIDA FLORES",
        "MyM mundo repuestos",
        "1725809121001",
        "JIPIJAPA AV. EL INCA E14-38 Y N47B DE LOS NOGALES",
        "001",
        "001",
        "000000001",
        null,
        false,
        false,
        30
    );
  }
}
