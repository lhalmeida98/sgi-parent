package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.ProductoCreateResult;
import ec.sgi.backend.application.dto.ProductoResult;
import ec.sgi.backend.application.port.in.CrearProductoCommand;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.ActualizarProductoCommand;
import ec.sgi.backend.application.port.in.ActualizarProductoUseCase;
import ec.sgi.backend.application.port.in.CrearProductoUseCase;
import ec.sgi.backend.application.port.in.ListarProductosUseCase;
import ec.sgi.backend.application.port.out.CategoriaRepository;
import ec.sgi.backend.application.port.out.ImpuestoRepository;
import ec.sgi.backend.application.port.out.ProductoRepository;
import ec.sgi.backend.domain.model.Producto;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductoService implements CrearProductoUseCase, ListarProductosUseCase, ActualizarProductoUseCase {
  private final ProductoRepository productoRepository;
  private final CategoriaRepository categoriaRepository;
  private final ImpuestoRepository impuestoRepository;

  public ProductoService(
      ProductoRepository productoRepository,
      CategoriaRepository categoriaRepository,
      ImpuestoRepository impuestoRepository
  ) {
    this.productoRepository = productoRepository;
    this.categoriaRepository = categoriaRepository;
    this.impuestoRepository = impuestoRepository;
  }

  @Override
  public ProductoCreateResult crear(CrearProductoCommand command) {
    validarReferencias(command.empresaId(), command.categoriaId(), command.impuestoId());
    Producto producto = new Producto(
        null,
        command.empresaId(),
        command.codigo(),
        command.descripcion(),
        command.precioUnitario(),
        command.categoriaId(),
        command.impuestoId(),
        command.codigoBarras()
    );
    Producto guardado = productoRepository.save(producto);
    return new ProductoCreateResult(guardado.id());
  }

  @Override
  public List<ProductoResult> listar(Long empresaId) {
    return productoRepository.findByEmpresaId(empresaId).stream()
        .map(this::toResult)
        .toList();
  }

  @Override
  public ProductoResult actualizar(Long empresaId, Long productoId, ActualizarProductoCommand command) {
    Producto existente = productoRepository.findByIdAndEmpresaId(productoId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    validarReferencias(empresaId, command.categoriaId(), command.impuestoId());
    Producto actualizado = new Producto(
        existente.id(),
        existente.empresaId(),
        command.codigo(),
        command.descripcion(),
        command.precioUnitario(),
        command.categoriaId(),
        command.impuestoId(),
        command.codigoBarras()
    );
    Producto guardado = productoRepository.save(actualizado);
    return toResult(guardado);
  }

  private ProductoResult toResult(Producto producto) {
    return new ProductoResult(
        producto.id(),
        producto.codigo(),
        producto.descripcion(),
        producto.precioUnitario(),
        producto.categoriaId(),
        producto.impuestoId(),
        producto.codigoBarras()
    );
  }

  private void validarReferencias(Long empresaId, Long categoriaId, Long impuestoId) {
    categoriaRepository.findByIdAndEmpresaId(categoriaId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));
    impuestoRepository.findByIdAndEmpresaId(impuestoId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Impuesto no encontrado"));
  }
}
