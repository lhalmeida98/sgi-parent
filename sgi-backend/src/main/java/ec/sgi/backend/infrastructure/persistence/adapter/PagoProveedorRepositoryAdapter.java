package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.PagoProveedorRepository;
import ec.sgi.backend.domain.model.PagoProveedor;
import ec.sgi.backend.domain.model.PagoProveedorDetalle;
import ec.sgi.backend.infrastructure.persistence.entity.PagoProveedorDetalleEntity;
import ec.sgi.backend.infrastructure.persistence.entity.PagoProveedorEntity;
import ec.sgi.backend.infrastructure.persistence.repository.PagoProveedorJpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PagoProveedorRepositoryAdapter implements PagoProveedorRepository {
  private final PagoProveedorJpaRepository pagoProveedorJpaRepository;

  public PagoProveedorRepositoryAdapter(PagoProveedorJpaRepository pagoProveedorJpaRepository) {
    this.pagoProveedorJpaRepository = pagoProveedorJpaRepository;
  }

  @Override
  public PagoProveedor save(PagoProveedor pago) {
    return toDomain(pagoProveedorJpaRepository.save(toEntity(pago)));
  }

  @Override
  public Optional<PagoProveedor> findById(Long id) {
    return pagoProveedorJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<PagoProveedor> findByIdAndEmpresaId(Long id, Long empresaId) {
    return pagoProveedorJpaRepository.findByIdAndEmpresaId(id, empresaId).map(this::toDomain);
  }

  @Override
  public List<PagoProveedor> findByEmpresaId(Long empresaId) {
    return pagoProveedorJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<PagoProveedor> findByProveedorId(Long proveedorId) {
    return pagoProveedorJpaRepository.findByProveedorId(proveedorId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<PagoProveedorRepository.FechaTotal> sumMontosPorFecha(
      Long empresaId,
      LocalDate fechaDesde,
      LocalDate fechaHasta
  ) {
    return pagoProveedorJpaRepository.sumMontosPorFecha(empresaId, fechaDesde, fechaHasta).stream()
        .map(projection -> new PagoProveedorRepository.FechaTotal(projection.getFecha(), projection.getTotal()))
        .toList();
  }

  private PagoProveedor toDomain(PagoProveedorEntity entity) {
    List<PagoProveedorDetalle> detalles = entity.getDetalles().stream()
        .map(detalle -> new PagoProveedorDetalle(
            detalle.getId(),
            detalle.getCuentaPorPagarId(),
            detalle.getMontoAplicado()
        ))
        .toList();
    return new PagoProveedor(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getProveedorId(),
        entity.getFechaPago(),
        entity.getFormaPago(),
        entity.getReferencia(),
        entity.getMontoTotal(),
        entity.getObservacion(),
        detalles,
        entity.getCreadoEn()
    );
  }

  private PagoProveedorEntity toEntity(PagoProveedor pago) {
    PagoProveedorEntity entity = new PagoProveedorEntity();
    entity.setId(pago.id());
    entity.setEmpresaId(pago.empresaId());
    entity.setProveedorId(pago.proveedorId());
    entity.setFechaPago(pago.fechaPago());
    entity.setFormaPago(pago.formaPago());
    entity.setReferencia(pago.referencia());
    entity.setMontoTotal(pago.montoTotal());
    entity.setObservacion(pago.observacion());
    if (pago.creadoEn() == null) {
      entity.setCreadoEn(LocalDateTime.now());
    } else {
      entity.setCreadoEn(pago.creadoEn());
    }

    List<PagoProveedorDetalleEntity> detalles = new ArrayList<>();
    for (PagoProveedorDetalle detalle : pago.detalles()) {
      PagoProveedorDetalleEntity detalleEntity = new PagoProveedorDetalleEntity();
      detalleEntity.setId(detalle.id());
      detalleEntity.setPagoProveedor(entity);
      detalleEntity.setCuentaPorPagarId(detalle.cuentaPorPagarId());
      detalleEntity.setMontoAplicado(detalle.montoAplicado());
      detalles.add(detalleEntity);
    }
    entity.setDetalles(detalles);
    return entity;
  }
}
