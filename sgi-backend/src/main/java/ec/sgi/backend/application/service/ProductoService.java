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
import ec.sgi.backend.application.port.out.BodegaRepository;
import ec.sgi.backend.application.port.out.CategoriaRepository;
import ec.sgi.backend.application.port.out.ImpuestoRepository;
import ec.sgi.backend.application.port.out.InventarioRepository;
import ec.sgi.backend.application.port.out.ProductoRepository;
import ec.sgi.backend.application.port.out.ProveedorRepository;
import ec.sgi.backend.domain.model.Inventario;
import ec.sgi.backend.domain.model.Producto;
import ec.sgi.backend.domain.model.Proveedor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductoService implements CrearProductoUseCase, ListarProductosUseCase,
    ActualizarProductoUseCase, ActualizarProductoVendibleUseCase, BuscarProductoPorCodigoUseCase {
  private static final String PROVEEDOR_IDENTIFICACION_DEFAULT = "SN-PROVEEDOR";
  private static final String PROVEEDOR_RAZON_SOCIAL_DEFAULT = "NO EXISTE";
  private static final String PROVEEDOR_TIPO_IDENTIFICACION_DEFAULT = "OTROS";
  private static final String PROVEEDOR_CONDICIONES_PAGO_DEFAULT = "CONTADO";
  private static final String PROVEEDOR_ESTADO_ACTIVO = "ACTIVO";

  private final ProductoRepository productoRepository;
  private final CategoriaRepository categoriaRepository;
  private final ImpuestoRepository impuestoRepository;
  private final ProveedorRepository proveedorRepository;
  private final BodegaRepository bodegaRepository;
  private final InventarioRepository inventarioRepository;

  public ProductoService(
      ProductoRepository productoRepository,
      CategoriaRepository categoriaRepository,
      ImpuestoRepository impuestoRepository,
      ProveedorRepository proveedorRepository,
      BodegaRepository bodegaRepository,
      InventarioRepository inventarioRepository
  ) {
    this.productoRepository = productoRepository;
    this.categoriaRepository = categoriaRepository;
    this.impuestoRepository = impuestoRepository;
    this.proveedorRepository = proveedorRepository;
    this.bodegaRepository = bodegaRepository;
    this.inventarioRepository = inventarioRepository;
  }

  @Override
  public ProductoCreateResult crear(CrearProductoCommand command) {
    validarReferencias(command.empresaId(), command.categoriaId(), command.impuestoId());
    Long proveedorId = resolveProveedorId(command.empresaId(), command.proveedorId());
    validarBodegaYCosto(command.empresaId(), command.bodegaId(), command.costo());
    boolean vendible = command.vendible() == null ? true : command.vendible();
    String codigoBarras = normalizeCodigoBarras(command.codigoBarras());
    Producto producto = new Producto(
        null,
        command.empresaId(),
        command.codigo(),
        command.descripcion(),
        command.precioUnitario(),
        command.categoriaId(),
        command.impuestoId(),
        proveedorId,
        vendible,
        codigoBarras
    );
    Producto guardado = productoRepository.save(producto);
    crearInventarioInicial(command.empresaId(), command.bodegaId(), guardado.id(), command.costo());
    return new ProductoCreateResult(guardado.id());
  }

  @Override
  public List<ProductoResult> listar(Long empresaId) {
    Map<Long, Proveedor> proveedoresPorId = proveedorRepository.findByEmpresaId(empresaId).stream()
        .collect(Collectors.toMap(Proveedor::id, Function.identity(), (a, b) -> a));
    return productoRepository.findByEmpresaId(empresaId).stream()
        .map(producto -> toResult(producto, proveedoresPorId.get(producto.proveedorId())))
        .toList();
  }

  @Override
  public ProductoResult actualizar(Long empresaId, Long productoId, ActualizarProductoCommand command) {
    Producto existente = productoRepository.findByIdAndEmpresaId(productoId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    validarReferencias(empresaId, command.categoriaId(), command.impuestoId());
    boolean vendible = command.vendible() == null ? existente.vendible() : command.vendible();
    String codigoBarras = normalizeCodigoBarras(command.codigoBarras());
    Producto actualizado = new Producto(
        existente.id(),
        existente.empresaId(),
        command.codigo(),
        command.descripcion(),
        command.precioUnitario(),
        command.categoriaId(),
        command.impuestoId(),
        existente.proveedorId(),
        vendible,
        codigoBarras
    );
    Producto guardado = productoRepository.save(actualizado);
    return toResult(guardado, loadProveedor(empresaId, guardado.proveedorId()));
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
        existente.proveedorId(),
        vendible,
        existente.codigoBarras()
    );
    Producto guardado = productoRepository.save(actualizado);
    return toResult(guardado, loadProveedor(empresaId, guardado.proveedorId()));
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
    return toResult(producto, loadProveedor(empresaId, producto.proveedorId()));
  }

  private ProductoResult toResult(Producto producto, Proveedor proveedor) {
    return new ProductoResult(
        producto.id(),
        producto.codigo(),
        producto.descripcion(),
        producto.precioUnitario(),
        producto.categoriaId(),
        producto.impuestoId(),
        producto.proveedorId(),
        proveedor == null ? null : proveedor.identificacion(),
        resolveProveedorNombre(proveedor),
        producto.vendible(),
        producto.codigoBarras()
    );
  }

  private Proveedor loadProveedor(Long empresaId, Long proveedorId) {
    if (proveedorId == null) {
      return null;
    }
    return proveedorRepository.findByIdAndEmpresaId(proveedorId, empresaId).orElse(null);
  }

  private String resolveProveedorNombre(Proveedor proveedor) {
    if (proveedor == null) {
      return null;
    }
    if (proveedor.razonSocial() != null && !proveedor.razonSocial().isBlank()) {
      return proveedor.razonSocial();
    }
    return proveedor.nombreComercial();
  }

  private String normalizeCodigoBarras(String codigoBarras) {
    if (codigoBarras == null) {
      return null;
    }
    String value = codigoBarras.trim();
    return value.isEmpty() ? null : value;
  }

  private void validarReferencias(Long empresaId, Long categoriaId, Long impuestoId) {
    categoriaRepository.findByIdAndEmpresaId(categoriaId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));
    impuestoRepository.findByIdAndEmpresaId(impuestoId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Impuesto no encontrado"));
  }

  private void validarBodegaYCosto(Long empresaId, Long bodegaId, BigDecimal costo) {
    bodegaRepository.findByIdAndEmpresaId(bodegaId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada"));
    if (costo.compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessRuleException("Costo invalido");
    }
  }

  private Long resolveProveedorId(Long empresaId, Long proveedorId) {
    if (proveedorId != null) {
      return proveedorRepository.findByIdAndEmpresaId(proveedorId, empresaId)
          .map(Proveedor::id)
          .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
    }
    return getOrCreateProveedorDefault(empresaId).id();
  }

  private Proveedor getOrCreateProveedorDefault(Long empresaId) {
    Proveedor existente = proveedorRepository.findByEmpresaIdAndIdentificacion(empresaId, PROVEEDOR_IDENTIFICACION_DEFAULT)
        .orElse(null);
    if (existente != null) {
      return existente;
    }
    try {
      return proveedorRepository.save(new Proveedor(
          null,
          empresaId,
          PROVEEDOR_TIPO_IDENTIFICACION_DEFAULT,
          PROVEEDOR_IDENTIFICACION_DEFAULT,
          PROVEEDOR_RAZON_SOCIAL_DEFAULT,
          PROVEEDOR_RAZON_SOCIAL_DEFAULT,
          null,
          null,
          null,
          PROVEEDOR_CONDICIONES_PAGO_DEFAULT,
          PROVEEDOR_ESTADO_ACTIVO,
          LocalDateTime.now(),
          null
      ));
    } catch (DataIntegrityViolationException ex) {
      return proveedorRepository.findByEmpresaIdAndIdentificacion(empresaId, PROVEEDOR_IDENTIFICACION_DEFAULT)
          .orElseThrow(() -> ex);
    }
  }

  private void crearInventarioInicial(Long empresaId, Long bodegaId, Long productoId, BigDecimal costo) {
    boolean existe = inventarioRepository.findByProductoIdAndEmpresaIdAndBodegaIdForUpdate(productoId, empresaId, bodegaId)
        .isPresent();
    if (existe) {
      throw new BusinessRuleException("Inventario ya existe para el producto en esa bodega");
    }
    inventarioRepository.save(new Inventario(
        null,
        empresaId,
        bodegaId,
        productoId,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        null,
        null,
        costo,
        LocalDateTime.now()
    ));
  }
}
