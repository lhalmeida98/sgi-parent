package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.FacturaRepository;
import ec.sgi.backend.application.dto.DashboardFacturaItemResult;
import ec.sgi.backend.application.dto.DashboardProductoVendidoResult;
import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.FacturaEstado;
import ec.sgi.backend.infrastructure.persistence.repository.FacturaJpaRepository;
import ec.sgi.backend.infrastructure.persistence.repository.FacturaItemJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class FacturaRepositoryAdapter implements FacturaRepository {
  private final FacturaJpaRepository facturaJpaRepository;
  private final FacturaItemJpaRepository facturaItemJpaRepository;
  private final FacturaEntityMapper mapper = new FacturaEntityMapper();

  public FacturaRepositoryAdapter(
      FacturaJpaRepository facturaJpaRepository,
      FacturaItemJpaRepository facturaItemJpaRepository
  ) {
    this.facturaJpaRepository = facturaJpaRepository;
    this.facturaItemJpaRepository = facturaItemJpaRepository;
  }

  @Override
  public Factura save(Factura factura) {
    if (factura.id() != null) {
      Optional<Factura> actualizada = facturaJpaRepository.findById(factura.id())
          .map(entity -> {
            mapper.updateEntityPreservingDetalle(entity, factura);
            return mapper.toDomain(facturaJpaRepository.save(entity));
          });
      if (actualizada.isPresent()) {
        return actualizada.get();
      }
    }
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
  public java.math.BigDecimal sumImporteTotalByEmpresaIdAndFechaEmisionBetweenAndEstado(
      Long empresaId,
      java.time.LocalDate fechaDesde,
      java.time.LocalDate fechaHasta,
      FacturaEstado estado
  ) {
    return facturaJpaRepository.sumImporteTotalByEmpresaIdAndFechaEmisionBetweenAndEstado(
        empresaId, fechaDesde, fechaHasta, estado.name());
  }

  @Override
  public List<DashboardFacturaItemResult> findUltimasFacturasResumen(
      Long empresaId,
      java.time.LocalDate fechaDesde,
      java.time.LocalDate fechaHasta,
      int limit
  ) {
    return facturaJpaRepository.findUltimasResumen(
        empresaId,
        fechaDesde,
        fechaHasta,
        PageRequest.of(0, limit)
    ).stream()
        .map(this::toDashboardFacturaItem)
        .toList();
  }

  @Override
  public List<DashboardProductoVendidoResult> findProductosMasVendidos(
      Long empresaId,
      java.time.LocalDate fechaDesde,
      java.time.LocalDate fechaHasta,
      FacturaEstado estado,
      int limit
  ) {
    return facturaItemJpaRepository.findProductosMasVendidos(
        empresaId,
        fechaDesde,
        fechaHasta,
        estado.name(),
        PageRequest.of(0, limit)
    ).stream()
        .map(item -> new DashboardProductoVendidoResult(
            item.getProductoId(),
            item.getDescripcion(),
            item.getCantidad(),
            item.getTotal()
        ))
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

  private DashboardFacturaItemResult toDashboardFacturaItem(
      FacturaJpaRepository.FacturaResumenProjection projection
  ) {
    String estab = projection.getInfoEstab() == null ? "" : projection.getInfoEstab();
    String ptoEmi = projection.getInfoPtoEmi() == null ? "" : projection.getInfoPtoEmi();
    String secuencial = projection.getInfoSecuencial() == null ? "" : projection.getInfoSecuencial();
    String numero = estab + "-" + ptoEmi + "-" + secuencial;
    return new DashboardFacturaItemResult(
        projection.getId(),
        numero,
        projection.getFechaEmision(),
        projection.getImporteTotal(),
        projection.getEstado()
    );
  }
}
