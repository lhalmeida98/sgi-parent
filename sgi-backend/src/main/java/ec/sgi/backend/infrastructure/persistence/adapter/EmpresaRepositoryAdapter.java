package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.EmpresaRepository;
import ec.sgi.backend.domain.model.Empresa;
import ec.sgi.backend.domain.model.RegimenTributario;
import ec.sgi.backend.infrastructure.persistence.entity.EmpresaEntity;
import ec.sgi.backend.infrastructure.persistence.repository.EmpresaJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class EmpresaRepositoryAdapter implements EmpresaRepository {
  private final EmpresaJpaRepository empresaJpaRepository;

  public EmpresaRepositoryAdapter(EmpresaJpaRepository empresaJpaRepository) {
    this.empresaJpaRepository = empresaJpaRepository;
  }

  @Override
  public Empresa save(Empresa empresa) {
    return toDomain(empresaJpaRepository.save(toEntity(empresa)));
  }

  @Override
  public Optional<Empresa> findById(Long id) {
    return empresaJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<Empresa> findByIdForUpdate(Long id) {
    return empresaJpaRepository.findByIdForUpdate(id).map(this::toDomain);
  }

  @Override
  public List<Empresa> findByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return empresaJpaRepository.findAllById(ids).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<Empresa> findAll() {
    return empresaJpaRepository.findAll().stream().map(this::toDomain).toList();
  }

  private Empresa toDomain(EmpresaEntity entity) {
    return new Empresa(
        entity.getId(),
        entity.getAmbiente(),
        entity.getTipoEmision(),
        entity.getRazonSocial(),
        entity.getNombreComercial(),
        entity.getRuc(),
        entity.getDirMatriz(),
        entity.getEstab(),
        entity.getPtoEmi(),
        entity.getSecuencial(),
        entity.getSecuencialPruebas(),
        entity.getLogoRuta(),
        entity.isObligadoContabilidad(),
        RegimenTributario.from(entity.getRegimenTributario(), entity.isRegimenRimpe()),
        entity.isContribuyenteEspecial(),
        entity.getNumeroContribuyenteEspecial(),
        entity.isAgenteRetencion(),
        entity.getCreditoDiasDefault()
    );
  }

  private EmpresaEntity toEntity(Empresa empresa) {
    EmpresaEntity entity = new EmpresaEntity();
    entity.setId(empresa.id());
    entity.setAmbiente(empresa.ambiente());
    entity.setTipoEmision(empresa.tipoEmision());
    entity.setRazonSocial(empresa.razonSocial());
    entity.setNombreComercial(empresa.nombreComercial());
    entity.setRuc(empresa.ruc());
    entity.setDirMatriz(empresa.dirMatriz());
    entity.setEstab(empresa.estab());
    entity.setPtoEmi(empresa.ptoEmi());
    entity.setSecuencial(empresa.secuencial());
    entity.setSecuencialPruebas(empresa.secuencialPruebas());
    entity.setLogoRuta(empresa.logoRuta());
    entity.setObligadoContabilidad(empresa.obligadoContabilidad());
    entity.setRegimenRimpe(empresa.regimenRimpe());
    entity.setRegimenTributario(empresa.regimenTributario().name());
    entity.setContribuyenteEspecial(empresa.contribuyenteEspecial());
    entity.setNumeroContribuyenteEspecial(empresa.numeroContribuyenteEspecial());
    entity.setAgenteRetencion(empresa.agenteRetencion());
    entity.setCreditoDiasDefault(empresa.creditoDiasDefault());
    return entity;
  }
}
