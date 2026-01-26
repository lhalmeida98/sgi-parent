package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.AccionCreateResult;
import ec.sgi.backend.application.dto.AccionResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.port.in.CrearAccionCommand;
import ec.sgi.backend.application.port.in.CrearAccionUseCase;
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
public class AccionService implements CrearAccionUseCase, ListarAccionesUseCase {
  private final AccionRepository accionRepository;

  public AccionService(AccionRepository accionRepository) {
    this.accionRepository = accionRepository;
  }

  @Override
  public AccionCreateResult crear(CrearAccionCommand command) {
    String codigo = normalizeCodigo(command.codigo());
    if (!codigo.matches("^[A-Z0-9_]+$")) {
      throw new BusinessRuleException("El codigo de accion debe usar solo A-Z, 0-9 y _");
    }
    if (accionRepository.existsByCodigo(command.empresaId(), codigo)) {
      throw new BusinessRuleException("La accion ya existe");
    }
    boolean activa = command.activo() == null || command.activo();
    Accion accion = new Accion(
        null,
        command.empresaId(),
        codigo,
        command.descripcion(),
        activa,
        LocalDateTime.now(),
        null
    );
    Accion guardada = accionRepository.save(accion);
    return new AccionCreateResult(guardada.id());
  }

  @Override
  public List<AccionResult> listar(Long empresaId) {
    return accionRepository.findByEmpresaId(empresaId).stream()
        .map(this::toResult)
        .toList();
  }

  private AccionResult toResult(Accion accion) {
    return new AccionResult(
        accion.id(),
        accion.codigo(),
        accion.descripcion(),
        accion.activo(),
        accion.creadoEn(),
        accion.actualizadoEn()
    );
  }

  private String normalizeCodigo(String codigo) {
    return codigo.trim().toUpperCase(Locale.ROOT);
  }
}
