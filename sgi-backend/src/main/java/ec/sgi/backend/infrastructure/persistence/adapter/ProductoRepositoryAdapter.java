package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.ProductoRepository;
import ec.sgi.backend.domain.model.Producto;
import ec.sgi.backend.infrastructure.persistence.entity.ProductoEntity;
import ec.sgi.backend.infrastructure.persistence.repository.ProductoJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ProductoRepositoryAdapter implements ProductoRepository {
  private final ProductoJpaRepository productoJpaRepository;

  public ProductoRepositoryAdapter(ProductoJpaRepository productoJpaRepository) {
    this.productoJpaRepository = productoJpaRepository;
  }

  @Override
  public Producto save(Producto producto) {
    return toDomain(productoJpaRepository.save(toEntity(producto)));
  }

  @Override
  public List<Producto> findAll() {
    return productoJpaRepository.findAll().stream().map(this::toDomain).toList();
  }

  @Override
  public Optional<Producto> findById(Long id) {
    return productoJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public List<Producto> findByEmpresaId(Long empresaId) {
    return productoJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Optional<Producto> findByIdAndEmpresaId(Long id, Long empresaId) {
    return productoJpaRepository.findByIdAndEmpresaId(id, empresaId).map(this::toDomain);
  }

  @Override
  public Optional<Producto> findByEmpresaIdAndCodigo(Long empresaId, String codigo) {
    return productoJpaRepository.findByEmpresaIdAndCodigo(empresaId, codigo).map(this::toDomain);
  }

  @Override
  public Optional<Producto> findByEmpresaIdAndCodigoBarras(Long empresaId, String codigoBarras) {
    return productoJpaRepository.findByEmpresaIdAndCodigoBarras(empresaId, codigoBarras).map(this::toDomain);
  }

  private Producto toDomain(ProductoEntity entity) {
    return new Producto(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getCodigo(),
        entity.getDescripcion(),
        entity.getPrecioUnitario(),
        entity.getCategoriaId(),
        entity.getImpuestoId(),
        entity.getVendible(),
        entity.getCodigoBarras()
    );
  }

  private ProductoEntity toEntity(Producto producto) {
    ProductoEntity entity = new ProductoEntity();
    entity.setId(producto.id());
    entity.setEmpresaId(producto.empresaId());
    entity.setCodigo(producto.codigo());
    entity.setDescripcion(producto.descripcion());
    entity.setPrecioUnitario(producto.precioUnitario());
    entity.setCategoriaId(producto.categoriaId());
    entity.setImpuestoId(producto.impuestoId());
    entity.setVendible(producto.vendible());
    entity.setCodigoBarras(producto.codigoBarras());
    return entity;
  }
}
