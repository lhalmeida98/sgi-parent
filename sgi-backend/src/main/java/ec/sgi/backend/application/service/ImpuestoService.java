package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.ImpuestoCreateResult;
import ec.sgi.backend.application.dto.ImpuestoResult;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.ActualizarImpuestoCommand;
import ec.sgi.backend.application.port.in.ActualizarImpuestoUseCase;
import ec.sgi.backend.application.port.in.CrearImpuestoCommand;
import ec.sgi.backend.application.port.in.CrearImpuestoUseCase;
import ec.sgi.backend.application.port.in.ListarImpuestosUseCase;
import ec.sgi.backend.application.port.out.ImpuestoRepository;
import ec.sgi.backend.domain.model.Impuesto;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ImpuestoService implements CrearImpuestoUseCase, ListarImpuestosUseCase, ActualizarImpuestoUseCase {
  private final ImpuestoRepository impuestoRepository;

  public ImpuestoService(ImpuestoRepository impuestoRepository) {
    this.impuestoRepository = impuestoRepository;
  }

  @Override
  public ImpuestoCreateResult crear(CrearImpuestoCommand command) {
    boolean activo = command.activo() == null || command.activo();
    Impuesto impuesto = new Impuesto(
        null,
        command.empresaId(),
        command.codigo(),
        command.codigoPorcentaje(),
        command.tarifa(),
        command.descripcion(),
        activo
    );
    Impuesto guardado = impuestoRepository.save(impuesto);
    return new ImpuestoCreateResult(guardado.id());
  }

  @Override
  public List<ImpuestoResult> listar(Long empresaId) {
    return impuestoRepository.findByEmpresaId(empresaId).stream()
        .map(this::toResult)
        .toList();
  }

  @Override
  public ImpuestoResult actualizar(Long empresaId, Long impuestoId, ActualizarImpuestoCommand command) {
    Impuesto existente = impuestoRepository.findByIdAndEmpresaId(impuestoId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Impuesto no encontrado"));
    boolean activo = command.activo() == null || command.activo();
    Impuesto actualizado = new Impuesto(
        existente.id(),
        existente.empresaId(),
        command.codigo(),
        command.codigoPorcentaje(),
        command.tarifa(),
        command.descripcion(),
        activo
    );
    Impuesto guardado = impuestoRepository.save(actualizado);
    return toResult(guardado);
  }

  private ImpuestoResult toResult(Impuesto impuesto) {
    return new ImpuestoResult(
        impuesto.id(),
        impuesto.codigo(),
        impuesto.codigoPorcentaje(),
        impuesto.tarifa(),
        impuesto.descripcion(),
        impuesto.activo()
    );
  }
}
