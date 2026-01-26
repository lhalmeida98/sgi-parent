package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.FirmaElectronicaRepository;
import ec.sgi.backend.domain.model.FirmaElectronica;
import ec.sgi.backend.infrastructure.persistence.entity.FirmaElectronicaEntity;
import ec.sgi.backend.infrastructure.persistence.repository.EmpresaJpaRepository;
import ec.sgi.backend.infrastructure.persistence.repository.FirmaElectronicaJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FirmaElectronicaRepositoryAdapter implements FirmaElectronicaRepository {
  private final FirmaElectronicaJpaRepository firmaJpaRepository;
  private final EmpresaJpaRepository empresaJpaRepository;

  public FirmaElectronicaRepositoryAdapter(
      FirmaElectronicaJpaRepository firmaJpaRepository,
      EmpresaJpaRepository empresaJpaRepository
  ) {
    this.firmaJpaRepository = firmaJpaRepository;
    this.empresaJpaRepository = empresaJpaRepository;
  }

  @Override
  public FirmaElectronica save(FirmaElectronica firma) {
    return toDomain(firmaJpaRepository.save(toEntity(firma)));
  }

  @Override
  public Optional<FirmaElectronica> findByEmpresaId(Long empresaId) {
    return firmaJpaRepository.findByEmpresaId(empresaId).map(this::toDomain);
  }

  private FirmaElectronica toDomain(FirmaElectronicaEntity entity) {
    Long empresaId = entity.getEmpresa().getId();
    return new FirmaElectronica(
        entity.getId(),
        empresaId,
        entity.getNombreArchivo(),
        entity.getTipoContenido(),
        entity.getRutaArchivo(),
        entity.getClave()
    );
  }

  private FirmaElectronicaEntity toEntity(FirmaElectronica firma) {
    FirmaElectronicaEntity entity = new FirmaElectronicaEntity();
    entity.setId(firma.id());
    entity.setEmpresa(empresaJpaRepository.getReferenceById(firma.empresaId()));
    entity.setNombreArchivo(firma.nombreArchivo());
    entity.setTipoContenido(firma.tipoContenido());
    entity.setRutaArchivo(firma.rutaArchivo());
    entity.setClave(firma.clave());
    return entity;
  }
}
