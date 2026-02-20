package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.RolCreateResult;
import ec.sgi.backend.application.dto.RolResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.ActualizarRolCommand;
import ec.sgi.backend.application.port.in.ActualizarRolUseCase;
import ec.sgi.backend.application.port.in.CrearRolCommand;
import ec.sgi.backend.application.port.in.CrearRolUseCase;
import ec.sgi.backend.application.port.in.EliminarRolUseCase;
import ec.sgi.backend.application.port.in.ListarRolesUseCase;
import ec.sgi.backend.application.port.out.AccionRepository;
import ec.sgi.backend.application.port.out.RolRepository;
import ec.sgi.backend.domain.model.Accion;
import ec.sgi.backend.domain.model.Rol;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RolService implements CrearRolUseCase, ListarRolesUseCase,
    ActualizarRolUseCase, EliminarRolUseCase {
  private final RolRepository rolRepository;
  private final AccionRepository accionRepository;

  public RolService(RolRepository rolRepository, AccionRepository accionRepository) {
    this.rolRepository = rolRepository;
    this.accionRepository = accionRepository;
  }

  @Override
  public RolCreateResult crear(CrearRolCommand command) {
    String nombre = normalizeRol(command.nombre());
    if (rolRepository.existsByNombre(nombre)) {
      throw new BusinessRuleException("El rol ya existe");
    }
    List<Long> accionesIds = normalizeAcciones(command.accionesIds());
    List<Long> accionesInvalidas = accionesIds.stream()
        .filter(id -> accionRepository.findById(id).filter(Accion::activo).isEmpty())
        .toList();
    if (!accionesInvalidas.isEmpty()) {
      throw new BusinessRuleException("Acciones invalidas o inactivas: " + accionesInvalidas);
    }
    boolean activo = command.activo() == null || command.activo();
    Rol rol = new Rol(null, nombre, command.descripcion(), activo, LocalDateTime.now(), null, accionesIds);
    Rol guardado = rolRepository.save(rol);
    return new RolCreateResult(guardado.id());
  }

  @Override
  public List<RolResult> listar() {
    return rolRepository.findAll().stream()
        .map(this::toResult)
        .toList();
  }

  @Override
  public RolResult actualizar(Long rolId, ActualizarRolCommand command) {
    Rol existente = rolRepository.findById(rolId)
        .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));
    String nombre = normalizeRol(command.nombre());
    rolRepository.findByNombre(nombre)
        .filter(rol -> !rol.id().equals(rolId))
        .ifPresent(rol -> {
          throw new BusinessRuleException("El rol ya existe");
        });
    List<Long> accionesIds = normalizeAcciones(command.accionesIds());
    List<Long> accionesInvalidas = accionesIds.stream()
        .filter(id -> accionRepository.findById(id).filter(Accion::activo).isEmpty())
        .toList();
    if (!accionesInvalidas.isEmpty()) {
      throw new BusinessRuleException("Acciones invalidas o inactivas: " + accionesInvalidas);
    }
    Rol actualizado = new Rol(
        existente.id(),
        nombre,
        command.descripcion(),
        command.activo(),
        existente.creadoEn(),
        LocalDateTime.now(),
        accionesIds
    );
    Rol guardado = rolRepository.save(actualizado);
    return toResult(guardado);
  }

  @Override
  public void eliminar(Long rolId) {
    if (rolRepository.findById(rolId).isEmpty()) {
      throw new ResourceNotFoundException("Rol no encontrado");
    }
    rolRepository.deleteById(rolId);
  }

  private RolResult toResult(Rol rol) {
    return new RolResult(
        rol.id(),
        rol.nombre(),
        rol.descripcion(),
        rol.activo(),
        rol.creadoEn(),
        rol.actualizadoEn(),
        rol.accionesIds()
    );
  }

  private String normalizeRol(String nombre) {
    return nombre.trim().toUpperCase(Locale.ROOT);
  }

  private List<Long> normalizeAcciones(List<Long> accionesIds) {
    Set<Long> normalizados = new LinkedHashSet<>();
    for (Long accionId : accionesIds) {
      if (accionId == null) {
        continue;
      }
      normalizados.add(accionId);
    }
    return new ArrayList<>(normalizados);
  }
}
