package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.InventarioBodegaResult;
import ec.sgi.backend.application.dto.InventarioCreateResult;
import ec.sgi.backend.application.dto.InventarioDetalleResult;
import ec.sgi.backend.application.dto.InventarioProductoDisponibleResult;
import ec.sgi.backend.application.dto.InventarioResumenResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.ActualizarInventarioCommand;
import ec.sgi.backend.application.port.in.ActualizarInventarioUseCase;
import ec.sgi.backend.application.port.in.CrearInventarioCommand;
import ec.sgi.backend.application.port.in.CrearInventarioUseCase;
import ec.sgi.backend.application.port.in.BuscarProductoDisponiblePorBodegaUseCase;
import ec.sgi.backend.application.port.in.BuscarProductoDisponiblePorIdUseCase;
import ec.sgi.backend.application.port.in.ConsultarInventarioProductoBodegaUseCase;
import ec.sgi.backend.application.port.in.ListarInventarioUseCase;
import ec.sgi.backend.application.port.in.ListarProductosDisponiblesPorBodegaUseCase;
import ec.sgi.backend.application.port.out.BodegaRepository;
import ec.sgi.backend.application.port.out.InventarioRepository;
import ec.sgi.backend.application.port.out.ProductoRepository;
import ec.sgi.backend.domain.model.Bodega;
import ec.sgi.backend.domain.model.Inventario;
import ec.sgi.backend.domain.model.Producto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventarioService implements CrearInventarioUseCase, ListarInventarioUseCase,
    ConsultarInventarioProductoBodegaUseCase, ActualizarInventarioUseCase, ListarProductosDisponiblesPorBodegaUseCase,
    BuscarProductoDisponiblePorBodegaUseCase, BuscarProductoDisponiblePorIdUseCase {
  private final InventarioRepository inventarioRepository;
  private final ProductoRepository productoRepository;
  private final BodegaRepository bodegaRepository;

  public InventarioService(
      InventarioRepository inventarioRepository,
      ProductoRepository productoRepository,
      BodegaRepository bodegaRepository
  ) {
    this.inventarioRepository = inventarioRepository;
    this.productoRepository = productoRepository;
    this.bodegaRepository = bodegaRepository;
  }

  @Override
  public InventarioCreateResult crear(CrearInventarioCommand command) {
    productoRepository.findByIdAndEmpresaId(command.productoId(), command.empresaId())
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    bodegaRepository.findByIdAndEmpresaId(command.bodegaId(), command.empresaId())
        .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada"));

    boolean existe = inventarioRepository.findByProductoIdAndEmpresaIdAndBodegaIdForUpdate(
        command.productoId(),
        command.empresaId(),
        command.bodegaId()
    ).isPresent();
    if (existe) {
      throw new BusinessRuleException("Inventario ya existe para el producto en esa bodega");
    }

    Inventario inventario = new Inventario(
        null,
        command.empresaId(),
        command.bodegaId(),
        command.productoId(),
        command.stockActual(),
        BigDecimal.ZERO,
        command.stockMinimo(),
        command.stockMaximo(),
        command.ubicacion(),
        command.costoPromedio(),
        LocalDateTime.now()
    );
    Inventario guardado = inventarioRepository.save(inventario);
    return new InventarioCreateResult(guardado.id());
  }

  @Override
  public InventarioDetalleResult actualizar(
      Long empresaId,
      Long productoId,
      Long bodegaId,
      ActualizarInventarioCommand command
  ) {
    if (command.stockActual().compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessRuleException("Stock actual invalido");
    }
    if (command.stockMinimo().compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessRuleException("Stock minimo invalido");
    }
    if (command.stockMaximo() != null && command.stockMaximo().compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessRuleException("Stock maximo invalido");
    }
    if (command.costoPromedio() != null && command.costoPromedio().compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessRuleException("Costo promedio invalido");
    }

    Inventario inventario = inventarioRepository.findByProductoIdAndEmpresaIdAndBodegaIdForUpdate(
        productoId,
        empresaId,
        bodegaId
    ).orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado"));

    Inventario actualizado = new Inventario(
        inventario.id(),
        inventario.empresaId(),
        inventario.bodegaId(),
        inventario.productoId(),
        command.stockActual(),
        inventario.stockReservado(),
        command.stockMinimo(),
        command.stockMaximo(),
        command.ubicacion(),
        command.costoPromedio(),
        LocalDateTime.now()
    );
    inventarioRepository.save(actualizado);
    return consultar(empresaId, productoId, bodegaId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<InventarioResumenResult> listar(Long empresaId) {
    List<Inventario> inventarios = inventarioRepository.findByEmpresaId(empresaId);
    Map<Long, Producto> productos = productoRepository.findByEmpresaId(empresaId).stream()
        .collect(Collectors.toMap(Producto::id, producto -> producto, (a, b) -> a));
    Map<Long, String> bodegaNombrePorId = bodegaRepository.findByEmpresaId(empresaId).stream()
        .collect(Collectors.toMap(Bodega::id, Bodega::nombre, (a, b) -> a));
    Map<Long, List<Inventario>> porProducto = inventarios.stream()
        .collect(Collectors.groupingBy(Inventario::productoId));

    List<InventarioResumenResult> resultados = new ArrayList<>();
    for (var entry : porProducto.entrySet()) {
      Long productoId = entry.getKey();
      Producto producto = productos.get(productoId);
      String productoNombre = producto == null ? null : producto.descripcion();
      BigDecimal precioVenta = producto == null ? null : producto.precioUnitario();
      List<Inventario> items = entry.getValue();
      BigDecimal stockGlobal = items.stream()
          .map(Inventario::stockActual)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      BigDecimal stockReservado = items.stream()
          .map(Inventario::stockReservado)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      BigDecimal costoPromedio = calcularCostoPromedioGlobal(items, stockGlobal);
      BigDecimal margenPorcentaje = calcularMargenPorcentaje(precioVenta, costoPromedio);
      List<InventarioBodegaResult> bodegas = items.stream()
          .map(inventario -> toBodegaResult(inventario, bodegaNombrePorId, precioVenta))
          .toList();
      resultados.add(new InventarioResumenResult(
          productoId,
          productoNombre,
          stockGlobal,
          stockReservado,
          costoPromedio,
          precioVenta,
          margenPorcentaje,
          bodegas
      ));
    }
    return resultados;
  }

  @Override
  @Transactional(readOnly = true)
  public InventarioDetalleResult consultar(Long empresaId, Long productoId, Long bodegaId) {
    Inventario inventario = inventarioRepository.findByProductoIdAndEmpresaIdAndBodegaId(productoId, empresaId, bodegaId)
        .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado"));
    Producto producto = productoRepository.findByIdAndEmpresaId(productoId, empresaId).orElse(null);
    Bodega bodega = bodegaRepository.findByIdAndEmpresaId(bodegaId, empresaId).orElse(null);
    List<Inventario> items = inventarioRepository.findByProductoIdAndEmpresaId(productoId, empresaId);
    BigDecimal stockGlobal = items.stream()
        .map(Inventario::stockActual)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal stockReservado = items.stream()
        .map(Inventario::stockReservado)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal costoPromedio = calcularCostoPromedioGlobal(items, stockGlobal);
    BigDecimal precioVenta = producto == null ? null : producto.precioUnitario();
    BigDecimal margenPorcentaje = calcularMargenPorcentaje(precioVenta, inventario.costoPromedio());
    BigDecimal margenPorcentajeGlobal = calcularMargenPorcentaje(precioVenta, costoPromedio);
    return new InventarioDetalleResult(
        productoId,
        producto == null ? null : producto.descripcion(),
        bodegaId,
        bodega == null ? null : bodega.nombre(),
        inventario.stockActual(),
        inventario.stockReservado(),
        inventario.stockMinimo(),
        inventario.stockMaximo(),
        inventario.ubicacion(),
        inventario.costoPromedio(),
        precioVenta,
        margenPorcentaje,
        stockGlobal,
        stockReservado,
        costoPromedio,
        margenPorcentajeGlobal
    );
  }

  @Override
  @Transactional(readOnly = true)
  public List<InventarioProductoDisponibleResult> listar(Long empresaId, Long bodegaId) {
    Bodega bodega = bodegaRepository.findByIdAndEmpresaId(bodegaId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada"));
    List<Inventario> inventarios = inventarioRepository.findByEmpresaIdAndBodegaId(empresaId, bodegaId);
    if (inventarios.isEmpty()) {
      return List.of();
    }
    Map<Long, Producto> productos = productoRepository.findByEmpresaId(empresaId).stream()
        .collect(Collectors.toMap(Producto::id, producto -> producto, (a, b) -> a));
    List<InventarioProductoDisponibleResult> resultados = new ArrayList<>();
    for (Inventario inventario : inventarios) {
      BigDecimal disponible = inventario.stockActual().subtract(inventario.stockReservado());
      if (disponible.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      Producto producto = productos.get(inventario.productoId());
      if (producto == null) {
        continue;
      }
      resultados.add(toDisponibleResult(producto, inventario, bodega.nombre(), disponible));
    }
    return resultados;
  }

  @Override
  @Transactional(readOnly = true)
  public InventarioProductoDisponibleResult buscar(Long empresaId, Long bodegaId, String codigo) {
    if (codigo == null || codigo.isBlank()) {
      throw new BusinessRuleException("Codigo requerido");
    }
    Bodega bodega = bodegaRepository.findByIdAndEmpresaId(bodegaId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada"));
    String value = codigo.trim();
    Producto producto = productoRepository.findByEmpresaIdAndCodigo(empresaId, value)
        .orElseGet(() -> productoRepository.findByEmpresaIdAndCodigoBarras(empresaId, value)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado")));
    Inventario inventario = inventarioRepository.findByProductoIdAndEmpresaIdAndBodegaId(
        producto.id(), empresaId, bodegaId
    ).orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado"));
    BigDecimal disponible = inventario.stockActual().subtract(inventario.stockReservado());
    if (disponible.compareTo(BigDecimal.ZERO) <= 0) {
      throw new ResourceNotFoundException("Producto sin stock disponible en la bodega");
    }
    return toDisponibleResult(producto, inventario, bodega.nombre(), disponible);
  }

  @Override
  @Transactional(readOnly = true)
  public InventarioProductoDisponibleResult buscar(Long empresaId, Long bodegaId, Long productoId) {
    Bodega bodega = bodegaRepository.findByIdAndEmpresaId(bodegaId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada"));
    Producto producto = productoRepository.findByIdAndEmpresaId(productoId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    Inventario inventario = inventarioRepository.findByProductoIdAndEmpresaIdAndBodegaId(
        productoId, empresaId, bodegaId
    ).orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado"));
    BigDecimal disponible = inventario.stockActual().subtract(inventario.stockReservado());
    if (disponible.compareTo(BigDecimal.ZERO) <= 0) {
      throw new ResourceNotFoundException("Producto sin stock disponible en la bodega");
    }
    return toDisponibleResult(producto, inventario, bodega.nombre(), disponible);
  }

  private InventarioBodegaResult toBodegaResult(
      Inventario inventario,
      Map<Long, String> bodegaNombrePorId,
      BigDecimal precioVenta
  ) {
    BigDecimal margenPorcentaje = calcularMargenPorcentaje(precioVenta, inventario.costoPromedio());
    return new InventarioBodegaResult(
        inventario.bodegaId(),
        bodegaNombrePorId.get(inventario.bodegaId()),
        inventario.stockActual(),
        inventario.stockReservado(),
        inventario.stockMinimo(),
        inventario.stockMaximo(),
        inventario.ubicacion(),
        inventario.costoPromedio(),
        margenPorcentaje
    );
  }

  private InventarioProductoDisponibleResult toDisponibleResult(
      Producto producto,
      Inventario inventario,
      String bodegaNombre,
      BigDecimal disponible
  ) {
    return new InventarioProductoDisponibleResult(
        producto.id(),
        producto.codigo(),
        producto.descripcion(),
        producto.precioUnitario(),
        producto.categoriaId(),
        producto.impuestoId(),
        producto.vendible(),
        producto.codigoBarras(),
        inventario.bodegaId(),
        bodegaNombre,
        inventario.stockActual(),
        inventario.stockReservado(),
        disponible,
        inventario.stockMinimo(),
        inventario.stockMaximo(),
        inventario.ubicacion(),
        inventario.costoPromedio()
    );
  }

  private BigDecimal calcularCostoPromedioGlobal(List<Inventario> items, BigDecimal stockGlobal) {
    if (stockGlobal.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal total = BigDecimal.ZERO;
    for (Inventario inv : items) {
      BigDecimal costo = inv.costoPromedio() == null ? BigDecimal.ZERO : inv.costoPromedio();
      total = total.add(costo.multiply(inv.stockActual()));
    }
    return total.divide(stockGlobal, 4, java.math.RoundingMode.HALF_UP);
  }

  private BigDecimal calcularMargenPorcentaje(BigDecimal precioVenta, BigDecimal costoPromedio) {
    if (precioVenta == null || costoPromedio == null) {
      return null;
    }
    if (costoPromedio.compareTo(BigDecimal.ZERO) <= 0) {
      return null;
    }
    BigDecimal utilidad = precioVenta.subtract(costoPromedio);
    return utilidad.multiply(BigDecimal.valueOf(100)).divide(costoPromedio, 2, RoundingMode.HALF_UP);
  }
}
