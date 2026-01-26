package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.ImpuestoRepository;
import ec.sgi.backend.domain.model.Impuesto;
import ec.sgi.backend.infrastructure.persistence.entity.ImpuestoEntity;
import ec.sgi.backend.infrastructure.persistence.repository.ImpuestoJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ImpuestoRepositoryAdapter implements ImpuestoRepository {
  private final ImpuestoJpaRepository impuestoJpaRepository;

  public ImpuestoRepositoryAdapter(ImpuestoJpaRepository impuestoJpaRepository) {
    this.impuestoJpaRepository = impuestoJpaRepository;
  }

  @Override
  public Impuesto save(Impuesto impuesto) {
    return toDomain(impuestoJpaRepository.save(toEntity(impuesto)));
  }

  @Override
  public Optional<Impuesto> findById(Long id) {
    return impuestoJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public List<Impuesto> findAll() {
    return impuestoJpaRepository.findAll().stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<Impuesto> findByEmpresaId(Long empresaId) {
    return impuestoJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Optional<Impuesto> findByIdAndEmpresaId(Long id, Long empresaId) {
    return impuestoJpaRepository.findByIdAndEmpresaId(id, empresaId).map(this::toDomain);
  }

  private Impuesto toDomain(ImpuestoEntity entity) {
    boolean activo = entity.getActivo() == null || entity.getActivo();
    return new Impuesto(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getCodigo(),
        entity.getCodigoPorcentaje(),
        entity.getTarifa(),
        entity.getDescripcion(),
        activo
    );
  }

  private ImpuestoEntity toEntity(Impuesto impuesto) {
    ImpuestoEntity entity = new ImpuestoEntity();
    entity.setId(impuesto.id());
    entity.setEmpresaId(impuesto.empresaId());
    entity.setCodigo(impuesto.codigo());
    entity.setCodigoPorcentaje(impuesto.codigoPorcentaje());
    entity.setTarifa(impuesto.tarifa());
    entity.setDescripcion(impuesto.descripcion());
    entity.setActivo(impuesto.activo());
    return entity;
  }
}
