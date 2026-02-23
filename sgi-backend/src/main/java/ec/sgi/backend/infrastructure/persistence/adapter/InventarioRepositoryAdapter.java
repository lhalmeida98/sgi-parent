package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.InventarioRepository;
import ec.sgi.backend.domain.model.Inventario;
import ec.sgi.backend.infrastructure.persistence.entity.InventarioEntity;
import ec.sgi.backend.infrastructure.persistence.repository.InventarioJpaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class InventarioRepositoryAdapter implements InventarioRepository {
  private final InventarioJpaRepository inventarioJpaRepository;

  public InventarioRepositoryAdapter(InventarioJpaRepository inventarioJpaRepository) {
    this.inventarioJpaRepository = inventarioJpaRepository;
  }

  @Override
  public Optional<Inventario> findByProductoIdForUpdate(Long productoId) {
    return inventarioJpaRepository.findByProductoId(productoId).map(this::toDomain);
  }

  @Override
  public Optional<Inventario> findByProductoIdAndEmpresaIdForUpdate(Long productoId, Long empresaId) {
    return inventarioJpaRepository.findByProductoIdAndEmpresaId(productoId, empresaId).map(this::toDomain);
  }

  @Override
  public Optional<Inventario> findByProductoIdAndEmpresaIdAndBodegaIdForUpdate(Long productoId, Long empresaId,
      Long bodegaId) {
    return inventarioJpaRepository.lockByProductoIdAndEmpresaIdAndBodegaId(productoId, empresaId, bodegaId)
        .map(this::toDomain);
  }

  @Override
  public List<Inventario> findByProductoIdAndEmpresaId(Long productoId, Long empresaId) {
    return inventarioJpaRepository.findAllByProductoIdAndEmpresaId(productoId, empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Optional<Inventario> findByProductoIdAndEmpresaIdAndBodegaId(Long productoId, Long empresaId, Long bodegaId) {
    return inventarioJpaRepository.findByProductoIdAndEmpresaIdAndBodegaId(productoId, empresaId, bodegaId)
        .map(this::toDomain);
  }

  @Override
  public List<Inventario> findByEmpresaIdAndBodegaId(Long empresaId, Long bodegaId) {
    return inventarioJpaRepository.findByEmpresaIdAndBodegaId(empresaId, bodegaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Inventario save(Inventario inventario) {
    return toDomain(inventarioJpaRepository.save(toEntity(inventario)));
  }

  @Override
  public List<Inventario> findAll() {
    return inventarioJpaRepository.findAll().stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<Inventario> findByEmpresaId(Long empresaId) {
    return inventarioJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public long countStockCriticoByEmpresaId(Long empresaId) {
    return inventarioJpaRepository.countStockCriticoByEmpresaId(empresaId);
  }

  private Inventario toDomain(InventarioEntity entity) {
    BigDecimal reservado = entity.getStockReservado() == null ? BigDecimal.ZERO : entity.getStockReservado();
    return new Inventario(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getBodegaId(),
        entity.getProductoId(),
        entity.getStockActual(),
        reservado,
        entity.getStockMinimo(),
        entity.getStockMaximo(),
        entity.getUbicacion(),
        entity.getCostoPromedio(),
        entity.getActualizadoEn()
    );
  }

  private InventarioEntity toEntity(Inventario inventario) {
    InventarioEntity entity = new InventarioEntity();
    entity.setId(inventario.id());
    entity.setEmpresaId(inventario.empresaId());
    entity.setBodegaId(inventario.bodegaId());
    entity.setProductoId(inventario.productoId());
    entity.setStockActual(inventario.stockActual());
    entity.setStockReservado(inventario.stockReservado());
    entity.setStockMinimo(inventario.stockMinimo());
    entity.setStockMaximo(inventario.stockMaximo());
    entity.setUbicacion(inventario.ubicacion());
    entity.setCostoPromedio(inventario.costoPromedio());
    entity.setActualizadoEn(inventario.actualizadoEn());
    return entity;
  }
}
