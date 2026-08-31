package ec.sgi.backend.interfaces.rest;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ec.sgi.backend.application.port.in.ActualizarUsuarioUseCase;
import ec.sgi.backend.application.port.in.CambiarEmpresaPrincipalUseCase;
import ec.sgi.backend.application.port.in.CrearUsuarioUseCase;
import ec.sgi.backend.application.port.in.EliminarUsuarioUseCase;
import ec.sgi.backend.application.port.in.ListarEmpresasUsuarioUseCase;
import ec.sgi.backend.application.port.in.ListarTodosUsuariosUseCase;
import ec.sgi.backend.application.port.in.ListarUsuariosUseCase;
import ec.sgi.backend.security.CurrentUserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {
  @Mock
  private CrearUsuarioUseCase crearUsuarioUseCase;
  @Mock
  private ListarUsuariosUseCase listarUsuariosUseCase;
  @Mock
  private ActualizarUsuarioUseCase actualizarUsuarioUseCase;
  @Mock
  private EliminarUsuarioUseCase eliminarUsuarioUseCase;
  @Mock
  private ListarEmpresasUsuarioUseCase listarEmpresasUsuarioUseCase;
  @Mock
  private CambiarEmpresaPrincipalUseCase cambiarEmpresaPrincipalUseCase;
  @Mock
  private ListarTodosUsuariosUseCase listarTodosUsuariosUseCase;
  @Mock
  private CurrentUserService currentUserService;

  private UsuarioController controller;

  @BeforeEach
  void setUp() {
    controller = new UsuarioController(
        crearUsuarioUseCase,
        listarUsuariosUseCase,
        actualizarUsuarioUseCase,
        eliminarUsuarioUseCase,
        listarEmpresasUsuarioUseCase,
        cambiarEmpresaPrincipalUseCase,
        listarTodosUsuariosUseCase,
        currentUserService
    );
  }

  @Test
  void listarEmpresasComoAdminNoFiltraPorEmpresaActual() {
    when(currentUserService.isAdmin()).thenReturn(true);
    when(listarEmpresasUsuarioUseCase.listarEmpresas(null, 4L)).thenReturn(List.of());

    controller.listarEmpresas(4L);

    verify(listarEmpresasUsuarioUseCase).listarEmpresas(null, 4L);
    verify(currentUserService, never()).getEmpresaId();
  }
}
