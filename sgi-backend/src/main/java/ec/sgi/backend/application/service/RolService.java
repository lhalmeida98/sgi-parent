package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.RolCreateResult;
import ec.sgi.backend.application.dto.RolResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.port.in.CrearRolCommand;
import ec.sgi.backend.application.port.in.CrearRolUseCase;
import ec.sgi.backend.application.port.in.ListarRolesUseCase;
import ec.sgi.backend.application.port.out.AccionRepository;
import ec.sgi.backend.application.port.out.RolRepository;
import ec.sgi.backend.domain.model.Rol;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RolService implements CrearRolUseCase, ListarRolesUseCase {
  private final RolRepository rolRepository;
  private final AccionRepository accionRepository;

  public RolService(RolRepository rolRepository, AccionRepository accionRepository) {
    this.rolRepository = rolRepository;
    this.accionRepository = accionRepository;
  }

  @Override
  public RolCreateResult crear(CrearRolCommand command) {
    String nombre = normalizeRol(command.nombre());
    if (rolRepository.existsByNombre(command.empresaId(), nombre)) {
      throw new BusinessRuleException("El rol ya existe");
    }
    List<String> permisos = normalizePermisos(command.permisos());
    List<String> permisosInvalidos = permisos.stream()
        .filter(permiso -> !accionRepository.existsActiveByCodigo(command.empresaId(), permiso))
        .toList();
    if (!permisosInvalidos.isEmpty()) {
      throw new BusinessRuleException("Permisos invalidos o inactivos: " + String.join(", ", permisosInvalidos));
    }
    Rol rol = new Rol(null, command.empresaId(), nombre, command.descripcion(), permisos);
    Rol guardado = rolRepository.save(rol);
    return new RolCreateResult(guardado.id());
  }

  @Override
  public List<RolResult> listar(Long empresaId) {
    return rolRepository.findByEmpresaId(empresaId).stream()
        .map(this::toResult)
        .toList();
  }

  private RolResult toResult(Rol rol) {
    return new RolResult(
        rol.id(),
        rol.nombre(),
        rol.descripcion(),
        rol.permisos()
    );
  }

  private String normalizeRol(String nombre) {
    return nombre.trim().toUpperCase(Locale.ROOT);
  }

  private List<String> normalizePermisos(List<String> permisos) {
    Set<String> normalizados = new LinkedHashSet<>();
    for (String permiso : permisos) {
      if (permiso == null || permiso.isBlank()) {
        continue;
      }
      normalizados.add(permiso.trim().toUpperCase(Locale.ROOT));
    }
    return new ArrayList<>(normalizados);
  }
}
