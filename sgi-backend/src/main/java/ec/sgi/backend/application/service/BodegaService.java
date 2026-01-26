package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.BodegaCreateResult;
import ec.sgi.backend.application.dto.BodegaResult;
import ec.sgi.backend.application.port.in.CrearBodegaCommand;
import ec.sgi.backend.application.port.in.CrearBodegaUseCase;
import ec.sgi.backend.application.port.in.ListarBodegasUseCase;
import ec.sgi.backend.application.port.out.BodegaRepository;
import ec.sgi.backend.domain.model.Bodega;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BodegaService implements CrearBodegaUseCase, ListarBodegasUseCase {
  private final BodegaRepository bodegaRepository;

  public BodegaService(BodegaRepository bodegaRepository) {
    this.bodegaRepository = bodegaRepository;
  }

  @Override
  public BodegaCreateResult crear(CrearBodegaCommand command) {
    boolean activa = command.activa() == null || command.activa();
    Bodega nueva = new Bodega(
        null,
        command.empresaId(),
        command.nombre(),
        command.descripcion(),
        command.direccion(),
        activa,
        LocalDateTime.now(),
        null
    );
    Bodega guardada = bodegaRepository.save(nueva);
    return new BodegaCreateResult(guardada.id());
  }

  @Override
  public List<BodegaResult> listar(Long empresaId) {
    return bodegaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toResult)
        .toList();
  }

  private BodegaResult toResult(Bodega bodega) {
    return new BodegaResult(
        bodega.id(),
        bodega.nombre(),
        bodega.descripcion(),
        bodega.direccion(),
        bodega.activa(),
        bodega.creadoEn(),
        bodega.actualizadoEn()
    );
  }
}
