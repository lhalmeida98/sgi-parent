package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.CuentaPorPagarRepository;
import ec.sgi.backend.domain.model.CuentaPorPagar;
import ec.sgi.backend.infrastructure.persistence.entity.CuentaPorPagarEntity;
import ec.sgi.backend.infrastructure.persistence.repository.CuentaPorPagarJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CuentaPorPagarRepositoryAdapter implements CuentaPorPagarRepository {
  private final CuentaPorPagarJpaRepository cuentaPorPagarJpaRepository;

  public CuentaPorPagarRepositoryAdapter(CuentaPorPagarJpaRepository cuentaPorPagarJpaRepository) {
    this.cuentaPorPagarJpaRepository = cuentaPorPagarJpaRepository;
  }

  @Override
  public CuentaPorPagar save(CuentaPorPagar cuenta) {
    return toDomain(cuentaPorPagarJpaRepository.save(toEntity(cuenta)));
  }

  @Override
  public Optional<CuentaPorPagar> findById(Long id) {
    return cuentaPorPagarJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<CuentaPorPagar> findByIdAndEmpresaId(Long id, Long empresaId) {
    return cuentaPorPagarJpaRepository.findByIdAndEmpresaId(id, empresaId).map(this::toDomain);
  }

  @Override
  public Optional<CuentaPorPagar> findByDocumentoProveedorId(Long documentoProveedorId) {
    return cuentaPorPagarJpaRepository.findByDocumentoProveedorId(documentoProveedorId).map(this::toDomain);
  }

  @Override
  public List<CuentaPorPagar> findByEmpresaId(Long empresaId) {
    return cuentaPorPagarJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<CuentaPorPagar> findByProveedorId(Long proveedorId) {
    return cuentaPorPagarJpaRepository.findByProveedorId(proveedorId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<CuentaPorPagar> findByProveedorIdAndEmpresaId(Long proveedorId, Long empresaId) {
    return cuentaPorPagarJpaRepository.findByProveedorIdAndEmpresaId(proveedorId, empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public BigDecimal sumSaldoPendienteByEmpresaId(Long empresaId, List<String> estadosExcluidos) {
    return cuentaPorPagarJpaRepository.sumSaldoPendienteByEmpresaId(empresaId, estadosExcluidos);
  }

  @Override
  public long countVencenEntreFechasByEmpresaId(
      Long empresaId,
      LocalDate fechaDesde,
      LocalDate fechaHasta,
      List<String> estadosExcluidos
  ) {
    return cuentaPorPagarJpaRepository.countVencenEntreFechasByEmpresaId(
        empresaId, fechaDesde, fechaHasta, estadosExcluidos);
  }

  private CuentaPorPagar toDomain(CuentaPorPagarEntity entity) {
    return new CuentaPorPagar(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getProveedorId(),
        entity.getDocumentoProveedorId(),
        entity.getMontoOriginal(),
        entity.getMontoPagado(),
        entity.getSaldo(),
        entity.getEstado(),
        entity.getFechaVencimiento(),
        entity.getCreadoEn(),
        entity.getActualizadoEn()
    );
  }

  private CuentaPorPagarEntity toEntity(CuentaPorPagar cuenta) {
    CuentaPorPagarEntity entity = new CuentaPorPagarEntity();
    entity.setId(cuenta.id());
    entity.setEmpresaId(cuenta.empresaId());
    entity.setProveedorId(cuenta.proveedorId());
    entity.setDocumentoProveedorId(cuenta.documentoProveedorId());
    entity.setMontoOriginal(cuenta.montoOriginal());
    entity.setMontoPagado(cuenta.montoPagado());
    entity.setSaldo(cuenta.saldo());
    entity.setEstado(cuenta.estado());
    entity.setFechaVencimiento(cuenta.fechaVencimiento());
    if (cuenta.creadoEn() == null) {
      entity.setCreadoEn(LocalDateTime.now());
    } else {
      entity.setCreadoEn(cuenta.creadoEn());
    }
    entity.setActualizadoEn(cuenta.actualizadoEn());
    return entity;
  }
}
