package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.AccionRepository;
import ec.sgi.backend.domain.model.Accion;
import ec.sgi.backend.infrastructure.persistence.entity.AccionEntity;
import ec.sgi.backend.infrastructure.persistence.repository.AccionJpaRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AccionRepositoryAdapter implements AccionRepository {
  private final AccionJpaRepository accionJpaRepository;

  public AccionRepositoryAdapter(AccionJpaRepository accionJpaRepository) {
    this.accionJpaRepository = accionJpaRepository;
  }

  @Override
  public Accion save(Accion accion) {
    return toDomain(accionJpaRepository.save(toEntity(accion)));
  }

  @Override
  public List<Accion> findAll() {
    return accionJpaRepository.findAll().stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Optional<Accion> findById(Long id) {
    return accionJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<Accion> findByCodigo(String codigo) {
    return accionJpaRepository.findByCodigo(codigo).map(this::toDomain);
  }

  @Override
  public List<Accion> findByCodigoIn(Collection<String> codigos) {
    if (codigos == null || codigos.isEmpty()) {
      return List.of();
    }
    return accionJpaRepository.findByCodigoIn(codigos).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public boolean existsByCodigo(String codigo) {
    return accionJpaRepository.existsByCodigo(codigo);
  }

  @Override
  public boolean existsActiveByCodigo(String codigo) {
    return accionJpaRepository.existsByCodigoAndActivoTrue(codigo);
  }

  @Override
  public void deleteById(Long id) {
    accionJpaRepository.deleteById(id);
  }

  private Accion toDomain(AccionEntity entity) {
    boolean activo = entity.getActivo() == null || entity.getActivo();
    return new Accion(
        entity.getId(),
        entity.getNombre(),
        entity.getCodigo(),
        entity.getDescripcion(),
        entity.getUrl(),
        entity.getIcono(),
        entity.getTipo(),
        activo,
        entity.getCreadoEn(),
        entity.getActualizadoEn()
    );
  }

  private AccionEntity toEntity(Accion accion) {
    AccionEntity entity = new AccionEntity();
    entity.setId(accion.id());
    entity.setNombre(accion.nombre());
    entity.setCodigo(accion.codigo());
    entity.setDescripcion(accion.descripcion());
    entity.setUrl(accion.url());
    entity.setIcono(accion.icono());
    entity.setTipo(accion.tipo());
    entity.setActivo(accion.activo());
    if (accion.creadoEn() == null) {
      entity.setCreadoEn(LocalDateTime.now());
    } else {
      entity.setCreadoEn(accion.creadoEn());
    }
    entity.setActualizadoEn(accion.actualizadoEn());
    return entity;
  }
}
