package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.FacturaRepository;
import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.FacturaEstado;
import ec.sgi.backend.infrastructure.persistence.repository.FacturaJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FacturaRepositoryAdapter implements FacturaRepository {
  private final FacturaJpaRepository facturaJpaRepository;
  private final FacturaEntityMapper mapper = new FacturaEntityMapper();

  public FacturaRepositoryAdapter(FacturaJpaRepository facturaJpaRepository) {
    this.facturaJpaRepository = facturaJpaRepository;
  }

  @Override
  public Factura save(Factura factura) {
    return mapper.toDomain(facturaJpaRepository.save(mapper.toEntity(factura)));
  }

  @Override
  public Optional<Factura> findById(Long id) {
    return facturaJpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Factura> findByEstado(FacturaEstado estado) {
    return facturaJpaRepository.findByEstado(estado.name()).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<Factura> findByEstadoAndEmpresaId(FacturaEstado estado, Long empresaId) {
    return facturaJpaRepository.findByEstadoAndEmpresaId(estado.name(), empresaId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<Factura> findByEmpresaId(Long empresaId) {
    return facturaJpaRepository.findByEmpresaId(empresaId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public org.springframework.data.domain.Page<Factura> findByEmpresaIdAndFechaEmisionBetween(
      Long empresaId,
      java.time.LocalDate fechaDesde,
      java.time.LocalDate fechaHasta,
      org.springframework.data.domain.Pageable pageable) {
    return facturaJpaRepository.findByEmpresaIdAndFechaEmisionBetween(empresaId, fechaDesde, fechaHasta, pageable)
        .map(mapper::toDomain);
  }

  @Override
  public Optional<Factura> findByEmpresaIdAndInfoEstabAndInfoPtoEmiAndInfoSecuencial(
      Long empresaId,
      String infoEstab,
      String infoPtoEmi,
      String infoSecuencial) {
    return facturaJpaRepository.findByEmpresaIdAndInfoEstabAndInfoPtoEmiAndInfoSecuencial(
        empresaId, infoEstab, infoPtoEmi, infoSecuencial).map(mapper::toDomain);
  }

  @Override
  public Optional<Factura> findByEmpresaIdAndInfoSecuencial(Long empresaId, String infoSecuencial) {
    return facturaJpaRepository.findByEmpresaIdAndInfoSecuencial(empresaId, infoSecuencial).map(mapper::toDomain);
  }
}
