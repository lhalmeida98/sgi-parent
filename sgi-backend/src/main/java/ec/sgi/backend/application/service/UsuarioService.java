package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.UsuarioCreateResult;
import ec.sgi.backend.application.dto.EmpresaResult;
import ec.sgi.backend.application.dto.UsuarioEmpresaDetalleResult;
import ec.sgi.backend.application.dto.UsuarioEmpresaResult;
import ec.sgi.backend.application.dto.UsuarioResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.ActualizarUsuarioCommand;
import ec.sgi.backend.application.port.in.ActualizarUsuarioUseCase;
import ec.sgi.backend.application.port.in.CambiarEmpresaPrincipalUseCase;
import ec.sgi.backend.application.port.in.CrearUsuarioCommand;
import ec.sgi.backend.application.port.in.CrearUsuarioUseCase;
import ec.sgi.backend.application.port.in.EliminarUsuarioUseCase;
import ec.sgi.backend.application.port.in.ListarEmpresasUsuarioUseCase;
import ec.sgi.backend.application.port.in.ListarTodosUsuariosUseCase;
import ec.sgi.backend.application.port.in.ListarUsuariosUseCase;
import ec.sgi.backend.application.port.out.EmpresaRepository;
import ec.sgi.backend.application.port.out.RolRepository;
import ec.sgi.backend.application.port.out.UsuarioRepository;
import ec.sgi.backend.domain.model.UsuarioEmpresa;
import ec.sgi.backend.domain.model.Usuario;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioService implements CrearUsuarioUseCase, ListarUsuariosUseCase,
    ActualizarUsuarioUseCase, EliminarUsuarioUseCase, ListarEmpresasUsuarioUseCase,
    CambiarEmpresaPrincipalUseCase, ListarTodosUsuariosUseCase {
  private final UsuarioRepository usuarioRepository;
  private final RolRepository rolRepository;
  private final EmpresaRepository empresaRepository;
  private final PasswordEncoder passwordEncoder;

  public UsuarioService(
      UsuarioRepository usuarioRepository,
      RolRepository rolRepository,
      EmpresaRepository empresaRepository,
      PasswordEncoder passwordEncoder
  ) {
    this.usuarioRepository = usuarioRepository;
    this.rolRepository = rolRepository;
    this.empresaRepository = empresaRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UsuarioCreateResult crear(CrearUsuarioCommand command) {
    usuarioRepository.findByEmail(command.email()).ifPresent(existente -> {
      throw new BusinessRuleException("El email ya esta registrado");
    });
    String usuario = normalizeUsuario(command.usuario());
    if (usuario.isBlank()) {
      throw new BusinessRuleException("Usuario requerido");
    }
    usuarioRepository.findByUsuario(usuario).ifPresent(existente -> {
      throw new BusinessRuleException("El usuario ya esta registrado");
    });
    List<String> roles = normalizeRoles(command.roles());
    validarRolesExisten(roles);
    List<UsuarioEmpresa> empresas = normalizeEmpresas(command.empresas());
    validarEmpresasExisten(empresas);
    Long empresaPrincipal = resolveEmpresaPrincipal(empresas);
    boolean activo = command.activo() == null || command.activo();
    LocalDateTime ahora = LocalDateTime.now();
    Usuario nuevo = new Usuario(
        null,
        empresaPrincipal,
        empresas,
        command.nombre(),
        usuario,
        command.email(),
        passwordEncoder.encode(command.password()),
        roles,
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
  public List<UsuarioResult> listarTodos() {
    return usuarioRepository.findAll().stream()
        .map(this::toResult)
        .toList();
  }

  @Override
  public UsuarioResult actualizar(Long empresaId, Long usuarioId, ActualizarUsuarioCommand command) {
    Usuario existente = (empresaId == null)
        ? usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"))
        : usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    usuarioRepository.findByEmail(command.email())
        .filter(usuario -> !usuario.id().equals(usuarioId))
        .ifPresent(usuario -> {
          throw new BusinessRuleException("El email ya esta registrado");
        });
    String usuario = normalizeUsuario(command.usuario());
    if (usuario.isBlank()) {
      throw new BusinessRuleException("Usuario requerido");
    }
    usuarioRepository.findByUsuario(usuario)
        .filter(encontrado -> !encontrado.id().equals(usuarioId))
        .ifPresent(encontrado -> {
          throw new BusinessRuleException("El usuario ya esta registrado");
        });
    List<String> roles = normalizeRoles(command.roles());
    validarRolesExisten(roles);
    List<UsuarioEmpresa> empresas = normalizeEmpresas(command.empresas());
    validarEmpresasExisten(empresas);
    Long empresaPrincipal = existente.empresaId();
    Long empresaPrincipalActual = empresaPrincipal;
    boolean contienePrincipal = empresaPrincipalActual != null
        && empresas.stream().anyMatch(emp -> emp.empresaId().equals(empresaPrincipalActual));
    if (!contienePrincipal) {
      empresaPrincipal = resolveEmpresaPrincipal(empresas);
    }
    String passwordHash = existente.passwordHash();
    if (command.password() != null && !command.password().isBlank()) {
      passwordHash = passwordEncoder.encode(command.password());
    }
    Usuario actualizado = new Usuario(
        existente.id(),
        empresaPrincipal,
        empresas,
        command.nombre(),
        usuario,
        command.email(),
        passwordHash,
        roles,
        command.activo(),
        existente.creadoEn(),
        LocalDateTime.now()
    );
    Usuario guardado = usuarioRepository.save(actualizado);
    return toResult(guardado);
  }

  @Override
  public void eliminar(Long empresaId, Long usuarioId) {
    Usuario existente = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    usuarioRepository.deleteById(existente.id());
  }

  @Override
  public List<UsuarioEmpresaDetalleResult> listarEmpresas(Long empresaId, Long usuarioId) {
    Usuario usuario = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    List<UsuarioEmpresa> empresas = usuario.empresas();
    if (empresas == null || empresas.isEmpty()) {
      return List.of();
    }
    List<Long> ids = empresas.stream().map(UsuarioEmpresa::empresaId).toList();
    java.util.Map<Long, EmpresaResult> detalles = empresaRepository.findByIds(ids).stream()
        .collect(java.util.stream.Collectors.toMap(
            ec.sgi.backend.domain.model.Empresa::id,
            this::toEmpresaResult
        ));
    List<UsuarioEmpresaDetalleResult> resultado = new ArrayList<>();
    for (UsuarioEmpresa empresa : empresas) {
      EmpresaResult detalle = detalles.get(empresa.empresaId());
      if (detalle == null) {
        continue;
      }
      resultado.add(new UsuarioEmpresaDetalleResult(detalle, empresa.principal()));
    }
    return resultado;
  }

  @Override
  public UsuarioResult cambiar(Long empresaId, Long usuarioId, Long empresaPrincipalId) {
    Usuario usuario = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    List<UsuarioEmpresa> empresas = usuario.empresas();
    if (empresas == null || empresas.isEmpty()) {
      throw new BusinessRuleException("El usuario no tiene empresas asignadas");
    }
    if (empresaPrincipalId == null) {
      throw new BusinessRuleException("Empresa principal requerida");
    }
    boolean pertenece = empresas.stream().anyMatch(emp -> emp.empresaId().equals(empresaPrincipalId));
    if (!pertenece) {
      throw new BusinessRuleException("La empresa no pertenece al usuario");
    }
    List<UsuarioEmpresa> actualizadas = empresas.stream()
        .map(emp -> new UsuarioEmpresa(emp.empresaId(), emp.empresaId().equals(empresaPrincipalId)))
        .toList();
    Usuario actualizado = new Usuario(
        usuario.id(),
        empresaPrincipalId,
        actualizadas,
        usuario.nombre(),
        usuario.usuario(),
        usuario.email(),
        usuario.passwordHash(),
        usuario.roles(),
        usuario.activo(),
        usuario.creadoEn(),
        LocalDateTime.now()
    );
    Usuario guardado = usuarioRepository.save(actualizado);
    return toResult(guardado);
  }

  private String normalizeUsuario(String usuario) {
    return usuario == null ? "" : usuario.trim();
  }

  private List<String> normalizeRoles(List<String> roles) {
    Set<String> normalizados = new LinkedHashSet<>();
    if (roles == null) {
      return List.of();
    }
    for (String rol : roles) {
      if (rol == null || rol.isBlank()) {
        continue;
      }
      normalizados.add(rol.trim().toUpperCase(Locale.ROOT));
    }
    return new ArrayList<>(normalizados);
  }

  private void validarRolesExisten(List<String> roles) {
    if (roles == null || roles.isEmpty()) {
      throw new BusinessRuleException("Roles requeridos");
    }
    for (String rol : roles) {
      if (!rolRepository.existsByNombre(rol)) {
        throw new BusinessRuleException("El rol no existe: " + rol);
      }
    }
  }

  private List<UsuarioEmpresa> normalizeEmpresas(List<UsuarioEmpresa> empresas) {
    if (empresas == null) {
      return List.of();
    }
    java.util.LinkedHashMap<Long, Boolean> mapa = new java.util.LinkedHashMap<>();
    for (UsuarioEmpresa empresa : empresas) {
      if (empresa == null || empresa.empresaId() == null) {
        continue;
      }
      boolean principal = empresa.principal();
      mapa.merge(
          empresa.empresaId(),
          principal,
          (oldVal, newVal) -> Boolean.TRUE.equals(oldVal) || Boolean.TRUE.equals(newVal)
      );
    }
    if (mapa.isEmpty()) {
      return List.of();
    }
    long principalCount = mapa.values().stream().filter(Boolean::booleanValue).count();
    if (principalCount > 1) {
      throw new BusinessRuleException("Solo una empresa puede ser principal");
    }
    if (principalCount == 0) {
      Long first = mapa.keySet().iterator().next();
      mapa.put(first, true);
    }
    List<UsuarioEmpresa> resultado = new ArrayList<>();
    for (java.util.Map.Entry<Long, Boolean> entry : mapa.entrySet()) {
      resultado.add(new UsuarioEmpresa(entry.getKey(), entry.getValue()));
    }
    return resultado;
  }

  private void validarEmpresasExisten(List<UsuarioEmpresa> empresas) {
    if (empresas == null || empresas.isEmpty()) {
      throw new BusinessRuleException("Empresas requeridas");
    }
    List<Long> invalidas = empresas.stream()
        .map(UsuarioEmpresa::empresaId)
        .filter(id -> empresaRepository.findById(id).isEmpty())
        .toList();
    if (!invalidas.isEmpty()) {
      throw new BusinessRuleException("Empresas invalidas: " + invalidas);
    }
  }

  private Long resolveEmpresaPrincipal(List<UsuarioEmpresa> empresas) {
    for (UsuarioEmpresa empresa : empresas) {
      if (empresa.principal()) {
        return empresa.empresaId();
      }
    }
    return empresas.get(0).empresaId();
  }

  private UsuarioResult toResult(Usuario usuario) {
    return new UsuarioResult(
        usuario.id(),
        usuario.nombre(),
        usuario.usuario(),
        usuario.email(),
        usuario.roles(),
        usuario.empresas().stream()
            .map(empresa -> new UsuarioEmpresaResult(empresa.empresaId(), empresa.principal()))
            .toList(),
        usuario.activo(),
        usuario.creadoEn(),
        usuario.actualizadoEn()
    );
  }

  private EmpresaResult toEmpresaResult(ec.sgi.backend.domain.model.Empresa empresa) {
    return new EmpresaResult(
        empresa.id(),
        empresa.ambiente(),
        empresa.tipoEmision(),
        empresa.razonSocial(),
        empresa.nombreComercial(),
        empresa.ruc(),
        empresa.dirMatriz(),
        empresa.estab(),
        empresa.ptoEmi(),
        empresa.secuencial(),
        empresa.logoRuta(),
        empresa.obligadoContabilidad(),
        empresa.regimenRimpe(),
        empresa.creditoDiasDefault()
    );
  }
}
