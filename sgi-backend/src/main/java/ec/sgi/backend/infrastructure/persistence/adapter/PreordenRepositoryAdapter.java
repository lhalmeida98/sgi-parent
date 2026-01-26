package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.PreordenRepository;
import ec.sgi.backend.domain.model.FacturaTotales;
import ec.sgi.backend.domain.model.Preorden;
import ec.sgi.backend.domain.model.PreordenItem;
import ec.sgi.backend.infrastructure.persistence.entity.PreordenEntity;
import ec.sgi.backend.infrastructure.persistence.entity.PreordenItemEntity;
import ec.sgi.backend.infrastructure.persistence.repository.PreordenJpaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PreordenRepositoryAdapter implements PreordenRepository {
  private final PreordenJpaRepository preordenJpaRepository;

  public PreordenRepositoryAdapter(PreordenJpaRepository preordenJpaRepository) {
    this.preordenJpaRepository = preordenJpaRepository;
  }

  @Override
  public Preorden save(Preorden preorden) {
    return toDomain(preordenJpaRepository.save(toEntity(preorden)));
  }

  @Override
  public Optional<Preorden> findById(Long id) {
    return preordenJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<Preorden> findByIdAndEmpresaId(Long id, Long empresaId) {
    return preordenJpaRepository.findByIdAndEmpresaId(id, empresaId).map(this::toDomain);
  }

  @Override
  public List<Preorden> findAll() {
    return preordenJpaRepository.findAll().stream().map(this::toDomain).toList();
  }

  @Override
  public List<Preorden> findByEmpresaId(Long empresaId) {
    return preordenJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  private Preorden toDomain(PreordenEntity entity) {
    FacturaTotales totales = new FacturaTotales(
        entity.getTotalSinImpuestos(),
        entity.getTotalDescuento(),
        entity.getTotalImpuestos(),
        entity.getImporteTotal()
    );

    List<PreordenItem> items = new ArrayList<>();
    for (PreordenItemEntity itemEntity : entity.getItems()) {
      items.add(new PreordenItem(
          itemEntity.getProductoId(),
          itemEntity.getCodigoPrincipal(),
          itemEntity.getDescripcion(),
          itemEntity.getCantidad(),
          itemEntity.getPrecioUnitario(),
          itemEntity.getDescuento(),
          itemEntity.getPrecioTotalSinImpuesto()
      ));
    }

    boolean reserva = entity.getReservaInventario() != null && entity.getReservaInventario();
    return new Preorden(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getClienteId(),
        entity.getFechaCreacion(),
        entity.getDirEstablecimiento(),
        entity.getMoneda(),
        totales,
        entity.getEstado(),
        entity.getObservaciones(),
        reserva,
        items
    );
  }

  private PreordenEntity toEntity(Preorden preorden) {
    PreordenEntity entity = new PreordenEntity();
    entity.setId(preorden.id());
    entity.setEmpresaId(preorden.empresaId());
    entity.setClienteId(preorden.clienteId());
    entity.setFechaCreacion(preorden.fechaCreacion());
    entity.setDirEstablecimiento(preorden.dirEstablecimiento());
    entity.setMoneda(preorden.moneda());
    entity.setTotalSinImpuestos(preorden.totales().totalSinImpuestos());
    entity.setTotalDescuento(preorden.totales().totalDescuento());
    entity.setTotalImpuestos(preorden.totales().totalImpuestos());
    entity.setImporteTotal(preorden.totales().importeTotal());
    entity.setEstado(preorden.estado());
    entity.setObservaciones(preorden.observaciones());
    entity.setReservaInventario(preorden.reservaInventario());

    List<PreordenItemEntity> itemEntities = new ArrayList<>();
    for (PreordenItem item : preorden.items()) {
      PreordenItemEntity itemEntity = new PreordenItemEntity();
      itemEntity.setPreorden(entity);
      itemEntity.setProductoId(item.productoId());
      itemEntity.setCodigoPrincipal(item.codigoPrincipal());
      itemEntity.setDescripcion(item.descripcion());
      itemEntity.setCantidad(item.cantidad());
      itemEntity.setPrecioUnitario(item.precioUnitario());
      itemEntity.setDescuento(item.descuento());
      itemEntity.setPrecioTotalSinImpuesto(item.precioTotalSinImpuesto());
      itemEntities.add(itemEntity);
    }
    entity.setItems(itemEntities);

    return entity;
  }
}
