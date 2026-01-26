package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.AccionRepository;
import ec.sgi.backend.domain.model.Accion;
import ec.sgi.backend.infrastructure.persistence.entity.AccionEntity;
import ec.sgi.backend.infrastructure.persistence.repository.AccionJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
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
  public List<Accion> findByEmpresaId(Long empresaId) {
    return accionJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public boolean existsByCodigo(Long empresaId, String codigo) {
    return accionJpaRepository.existsByEmpresaIdAndCodigo(empresaId, codigo);
  }

  @Override
  public boolean existsActiveByCodigo(Long empresaId, String codigo) {
    return accionJpaRepository.existsByEmpresaIdAndCodigoAndActivoTrue(empresaId, codigo);
  }

  private Accion toDomain(AccionEntity entity) {
    boolean activo = entity.getActivo() == null || entity.getActivo();
    return new Accion(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getCodigo(),
        entity.getDescripcion(),
        activo,
        entity.getCreadoEn(),
        entity.getActualizadoEn()
    );
  }

  private AccionEntity toEntity(Accion accion) {
    AccionEntity entity = new AccionEntity();
    entity.setId(accion.id());
    entity.setEmpresaId(accion.empresaId());
    entity.setCodigo(accion.codigo());
    entity.setDescripcion(accion.descripcion());
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
