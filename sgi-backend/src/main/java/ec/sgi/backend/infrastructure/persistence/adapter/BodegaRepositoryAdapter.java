package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.BodegaRepository;
import ec.sgi.backend.domain.model.Bodega;
import ec.sgi.backend.infrastructure.persistence.entity.BodegaEntity;
import ec.sgi.backend.infrastructure.persistence.repository.BodegaJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class BodegaRepositoryAdapter implements BodegaRepository {
  private final BodegaJpaRepository bodegaJpaRepository;

  public BodegaRepositoryAdapter(BodegaJpaRepository bodegaJpaRepository) {
    this.bodegaJpaRepository = bodegaJpaRepository;
  }

  @Override
  public Bodega save(Bodega bodega) {
    return toDomain(bodegaJpaRepository.save(toEntity(bodega)));
  }

  @Override
  public List<Bodega> findByEmpresaId(Long empresaId) {
    return bodegaJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Optional<Bodega> findByIdAndEmpresaId(Long id, Long empresaId) {
    return bodegaJpaRepository.findByIdAndEmpresaId(id, empresaId).map(this::toDomain);
  }

  private Bodega toDomain(BodegaEntity entity) {
    boolean activa = entity.getActiva() == null || entity.getActiva();
    return new Bodega(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getNombre(),
        entity.getDescripcion(),
        entity.getDireccion(),
        activa,
        entity.getCreadoEn(),
        entity.getActualizadoEn()
    );
  }

  private BodegaEntity toEntity(Bodega bodega) {
    BodegaEntity entity = new BodegaEntity();
    entity.setId(bodega.id());
    entity.setEmpresaId(bodega.empresaId());
    entity.setNombre(bodega.nombre());
    entity.setDescripcion(bodega.descripcion());
    entity.setDireccion(bodega.direccion());
    entity.setActiva(bodega.activa());
    if (bodega.creadoEn() == null) {
      entity.setCreadoEn(LocalDateTime.now());
    } else {
      entity.setCreadoEn(bodega.creadoEn());
    }
    entity.setActualizadoEn(bodega.actualizadoEn());
    return entity;
  }
}
