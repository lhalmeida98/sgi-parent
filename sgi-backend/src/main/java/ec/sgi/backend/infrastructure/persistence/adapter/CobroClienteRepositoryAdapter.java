package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.CobroClienteRepository;
import ec.sgi.backend.domain.model.CobroCliente;
import ec.sgi.backend.domain.model.CobroClienteDetalle;
import ec.sgi.backend.infrastructure.persistence.entity.CobroClienteDetalleEntity;
import ec.sgi.backend.infrastructure.persistence.entity.CobroClienteEntity;
import ec.sgi.backend.infrastructure.persistence.repository.CobroClienteJpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CobroClienteRepositoryAdapter implements CobroClienteRepository {
  private final CobroClienteJpaRepository cobroClienteJpaRepository;

  public CobroClienteRepositoryAdapter(CobroClienteJpaRepository cobroClienteJpaRepository) {
    this.cobroClienteJpaRepository = cobroClienteJpaRepository;
  }

  @Override
  public CobroCliente save(CobroCliente cobro) {
    return toDomain(cobroClienteJpaRepository.save(toEntity(cobro)));
  }

  @Override
  public Optional<CobroCliente> findById(Long id) {
    return cobroClienteJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<CobroCliente> findByIdAndEmpresaId(Long id, Long empresaId) {
    return cobroClienteJpaRepository.findByIdAndEmpresaId(id, empresaId).map(this::toDomain);
  }

  @Override
  public List<CobroCliente> findByEmpresaId(Long empresaId) {
    return cobroClienteJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<CobroCliente> findByClienteId(Long clienteId) {
    return cobroClienteJpaRepository.findByClienteId(clienteId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<CobroClienteRepository.FechaTotal> sumMontosPorFecha(
      Long empresaId,
      LocalDate fechaDesde,
      LocalDate fechaHasta
  ) {
    return cobroClienteJpaRepository.sumMontosPorFecha(empresaId, fechaDesde, fechaHasta).stream()
        .map(projection -> new CobroClienteRepository.FechaTotal(projection.getFecha(), projection.getTotal()))
        .toList();
  }

  private CobroCliente toDomain(CobroClienteEntity entity) {
    List<CobroClienteDetalle> detalles = entity.getDetalles().stream()
        .map(detalle -> new CobroClienteDetalle(
            detalle.getId(),
            detalle.getCuentaPorCobrarId(),
            detalle.getMontoAplicado()
        ))
        .toList();
    return new CobroCliente(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getClienteId(),
        entity.getFecha(),
        entity.getFormaPago(),
        entity.getReferencia(),
        entity.getMontoTotal(),
        entity.getObservacion(),
        detalles,
        entity.getCreadoEn()
    );
  }

  private CobroClienteEntity toEntity(CobroCliente cobro) {
    CobroClienteEntity entity = new CobroClienteEntity();
    entity.setId(cobro.id());
    entity.setEmpresaId(cobro.empresaId());
    entity.setClienteId(cobro.clienteId());
    entity.setFecha(cobro.fecha());
    entity.setFormaPago(cobro.formaPago());
    entity.setReferencia(cobro.referencia());
    entity.setMontoTotal(cobro.montoTotal());
    entity.setObservacion(cobro.observacion());
    if (cobro.creadoEn() == null) {
      entity.setCreadoEn(LocalDateTime.now());
    } else {
      entity.setCreadoEn(cobro.creadoEn());
    }

    List<CobroClienteDetalleEntity> detalles = new ArrayList<>();
    for (CobroClienteDetalle detalle : cobro.detalles()) {
      CobroClienteDetalleEntity detalleEntity = new CobroClienteDetalleEntity();
      detalleEntity.setId(detalle.id());
      detalleEntity.setCobroCliente(entity);
      detalleEntity.setCuentaPorCobrarId(detalle.cuentaPorCobrarId());
      detalleEntity.setMontoAplicado(detalle.montoAplicado());
      detalles.add(detalleEntity);
    }
    entity.setDetalles(detalles);
    return entity;
  }
}
