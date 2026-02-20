package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.AccionCreateResult;
import ec.sgi.backend.application.dto.AccionResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.ActualizarAccionCommand;
import ec.sgi.backend.application.port.in.ActualizarAccionUseCase;
import ec.sgi.backend.application.port.in.CrearAccionCommand;
import ec.sgi.backend.application.port.in.CrearAccionUseCase;
import ec.sgi.backend.application.port.in.EliminarAccionUseCase;
import ec.sgi.backend.application.port.in.ListarAccionesUseCase;
import ec.sgi.backend.application.port.out.AccionRepository;
import ec.sgi.backend.domain.model.Accion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccionService implements CrearAccionUseCase, ListarAccionesUseCase,
    ActualizarAccionUseCase, EliminarAccionUseCase {
  private final AccionRepository accionRepository;

  public AccionService(AccionRepository accionRepository) {
    this.accionRepository = accionRepository;
  }

  @Override
  public AccionCreateResult crear(CrearAccionCommand command) {
    String codigo = normalizeCodigo(command.codigo());
    String nombre = normalizeNombre(command.nombre());
    if (nombre.isBlank()) {
      throw new BusinessRuleException("Nombre requerido");
    }
    if (!codigo.matches("^[A-Z0-9_]+$")) {
      throw new BusinessRuleException("El codigo de accion debe usar solo A-Z, 0-9 y _");
    }
    if (accionRepository.existsByCodigo(codigo)) {
      throw new BusinessRuleException("La accion ya existe");
    }
    boolean activa = command.activo() == null || command.activo();
    Accion accion = new Accion(
        null,
        nombre,
        codigo,
        command.descripcion(),
        command.url(),
        command.icono(),
        command.tipo(),
        activa,
        LocalDateTime.now(),
        null
    );
    Accion guardada = accionRepository.save(accion);
    return new AccionCreateResult(guardada.id());
  }

  @Override
  public List<AccionResult> listar() {
    return accionRepository.findAll().stream()
        .map(this::toResult)
        .toList();
  }

  @Override
  public AccionResult actualizar(Long accionId, ActualizarAccionCommand command) {
    Accion existente = accionRepository.findById(accionId)
        .orElseThrow(() -> new ResourceNotFoundException("Accion no encontrada"));
    String codigo = normalizeCodigo(command.codigo());
    if (!codigo.equalsIgnoreCase(existente.codigo())) {
      throw new BusinessRuleException("No se permite cambiar la clave de la accion");
    }
    String nombre = normalizeNombre(command.nombre());
    if (nombre.isBlank()) {
      throw new BusinessRuleException("Nombre requerido");
    }
    Accion actualizado = new Accion(
        existente.id(),
        nombre,
        existente.codigo(),
        command.descripcion(),
        command.url(),
        command.icono(),
        command.tipo(),
        command.activo(),
        existente.creadoEn(),
        LocalDateTime.now()
    );
    Accion guardada = accionRepository.save(actualizado);
    return toResult(guardada);
  }

  @Override
  public void eliminar(Long accionId) {
    if (accionRepository.findById(accionId).isEmpty()) {
      throw new ResourceNotFoundException("Accion no encontrada");
    }
    accionRepository.deleteById(accionId);
  }

  private AccionResult toResult(Accion accion) {
    return new AccionResult(
        accion.id(),
        accion.nombre(),
        accion.codigo(),
        accion.descripcion(),
        accion.url(),
        accion.icono(),
        accion.tipo(),
        accion.activo(),
        accion.creadoEn(),
        accion.actualizadoEn()
    );
  }

  private String normalizeCodigo(String codigo) {
    return codigo.trim().toUpperCase(Locale.ROOT);
  }

  private String normalizeNombre(String nombre) {
    return nombre == null ? "" : nombre.trim();
  }
}
