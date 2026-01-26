package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.UsuarioCreateResult;
import ec.sgi.backend.application.dto.UsuarioResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.ActualizarUsuarioCommand;
import ec.sgi.backend.application.port.in.ActualizarUsuarioUseCase;
import ec.sgi.backend.application.port.in.CrearUsuarioCommand;
import ec.sgi.backend.application.port.in.CrearUsuarioUseCase;
import ec.sgi.backend.application.port.in.ListarUsuariosUseCase;
import ec.sgi.backend.application.port.out.RolRepository;
import ec.sgi.backend.application.port.out.UsuarioRepository;
import ec.sgi.backend.domain.model.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioService implements CrearUsuarioUseCase, ListarUsuariosUseCase, ActualizarUsuarioUseCase {
  private final UsuarioRepository usuarioRepository;
  private final RolRepository rolRepository;
  private final PasswordEncoder passwordEncoder;

  public UsuarioService(
      UsuarioRepository usuarioRepository,
      RolRepository rolRepository,
      PasswordEncoder passwordEncoder
  ) {
    this.usuarioRepository = usuarioRepository;
    this.rolRepository = rolRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UsuarioCreateResult crear(CrearUsuarioCommand command) {
    usuarioRepository.findByEmail(command.email()).ifPresent(existente -> {
      throw new BusinessRuleException("El email ya esta registrado");
    });
    String rol = normalizeRol(command.rol());
    validarRolExiste(command.empresaId(), rol);
    boolean activo = command.activo() == null || command.activo();
    LocalDateTime ahora = LocalDateTime.now();
    Usuario nuevo = new Usuario(
        null,
        command.empresaId(),
        command.nombre(),
        command.email(),
        passwordEncoder.encode(command.password()),
        rol,
        activo,
        ahora,
        null
    );
    Usuario guardado = usuarioRepository.save(nuevo);
    return new UsuarioCreateResult(guardado.id());
  }

  @Override
  public List<UsuarioResult> listar(Long empresaId) {
    return usuarioRepository.findByEmpresaId(empresaId).stream()
        .map(this::toResult)
        .toList();
  }

  @Override
  public UsuarioResult actualizar(Long empresaId, Long usuarioId, ActualizarUsuarioCommand command) {
    Usuario existente = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    usuarioRepository.findByEmail(command.email())
        .filter(usuario -> !usuario.id().equals(usuarioId))
        .ifPresent(usuario -> {
          throw new BusinessRuleException("El email ya esta registrado");
        });
    String rol = normalizeRol(command.rol());
    validarRolExiste(empresaId, rol);
    String passwordHash = existente.passwordHash();
    if (command.password() != null && !command.password().isBlank()) {
      passwordHash = passwordEncoder.encode(command.password());
    }
    Usuario actualizado = new Usuario(
        existente.id(),
        existente.empresaId(),
        command.nombre(),
        command.email(),
        passwordHash,
        rol,
        command.activo(),
        existente.creadoEn(),
        LocalDateTime.now()
    );
    Usuario guardado = usuarioRepository.save(actualizado);
    return toResult(guardado);
  }

  private String normalizeRol(String rol) {
    return rol.trim().toUpperCase(Locale.ROOT);
  }

  private void validarRolExiste(Long empresaId, String rol) {
    if ("ADMIN".equalsIgnoreCase(rol)) {
      return;
    }
    if (!rolRepository.existsByNombre(empresaId, rol)) {
      throw new BusinessRuleException("El rol no existe");
    }
  }

  private UsuarioResult toResult(Usuario usuario) {
    return new UsuarioResult(
        usuario.id(),
        usuario.nombre(),
        usuario.email(),
        usuario.rol(),
        usuario.activo(),
        usuario.creadoEn(),
        usuario.actualizadoEn()
    );
  }
}
