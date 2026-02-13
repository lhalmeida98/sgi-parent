package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.ProductoCreateResult;
import ec.sgi.backend.application.dto.ProductoResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.port.in.CrearProductoCommand;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.ActualizarProductoCommand;
import ec.sgi.backend.application.port.in.ActualizarProductoUseCase;
import ec.sgi.backend.application.port.in.ActualizarProductoVendibleUseCase;
import ec.sgi.backend.application.port.in.BuscarProductoPorCodigoUseCase;
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
public class ProductoService implements CrearProductoUseCase, ListarProductosUseCase,
    ActualizarProductoUseCase, ActualizarProductoVendibleUseCase, BuscarProductoPorCodigoUseCase {
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
    boolean vendible = command.vendible() == null ? true : command.vendible();
    Producto producto = new Producto(
        null,
        command.empresaId(),
        command.codigo(),
        command.descripcion(),
        command.precioUnitario(),
        command.categoriaId(),
        command.impuestoId(),
        vendible,
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
    boolean vendible = command.vendible() == null ? existente.vendible() : command.vendible();
    Producto actualizado = new Producto(
        existente.id(),
        existente.empresaId(),
        command.codigo(),
        command.descripcion(),
        command.precioUnitario(),
        command.categoriaId(),
        command.impuestoId(),
        vendible,
        command.codigoBarras()
    );
    Producto guardado = productoRepository.save(actualizado);
    return toResult(guardado);
  }

  @Override
  public ProductoResult actualizarVendible(Long empresaId, Long productoId, boolean vendible) {
    Producto existente = productoRepository.findByIdAndEmpresaId(productoId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    Producto actualizado = new Producto(
        existente.id(),
        existente.empresaId(),
        existente.codigo(),
        existente.descripcion(),
        existente.precioUnitario(),
        existente.categoriaId(),
        existente.impuestoId(),
        vendible,
        existente.codigoBarras()
    );
    Producto guardado = productoRepository.save(actualizado);
    return toResult(guardado);
  }

  @Override
  @Transactional(readOnly = true)
  public ProductoResult buscar(Long empresaId, String codigo) {
    if (codigo == null || codigo.isBlank()) {
      throw new BusinessRuleException("Codigo requerido");
    }
    String value = codigo.trim();
    Producto producto = productoRepository.findByEmpresaIdAndCodigo(empresaId, value)
        .orElseGet(() -> productoRepository.findByEmpresaIdAndCodigoBarras(empresaId, value)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado")));
    return toResult(producto);
  }

  private ProductoResult toResult(Producto producto) {
    return new ProductoResult(
        producto.id(),
        producto.codigo(),
        producto.descripcion(),
        producto.precioUnitario(),
        producto.categoriaId(),
        producto.impuestoId(),
        producto.vendible(),
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
