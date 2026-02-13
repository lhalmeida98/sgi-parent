package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.FacturaTotalesDto;
import ec.sgi.backend.application.dto.PreordenCreateResult;
import ec.sgi.backend.application.dto.PreordenItemResult;
import ec.sgi.backend.application.dto.PreordenResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.CrearPreordenCommand;
import ec.sgi.backend.application.port.in.CrearPreordenUseCase;
import ec.sgi.backend.application.port.in.ListarPreordenesUseCase;
import ec.sgi.backend.application.port.out.ClienteRepository;
import ec.sgi.backend.application.port.out.BodegaRepository;
import ec.sgi.backend.application.port.out.EmpresaRepository;
import ec.sgi.backend.application.port.out.ImpuestoRepository;
import ec.sgi.backend.application.port.out.InventarioRepository;
import ec.sgi.backend.application.port.out.PreordenRepository;
import ec.sgi.backend.application.port.out.ProductoRepository;
import ec.sgi.backend.domain.model.FacturaItem;
import ec.sgi.backend.domain.model.FacturaTotales;
import ec.sgi.backend.domain.model.Impuesto;
import ec.sgi.backend.domain.model.Inventario;
import ec.sgi.backend.domain.model.Preorden;
import ec.sgi.backend.domain.model.PreordenItem;
import ec.sgi.backend.domain.model.Producto;
import ec.sgi.backend.domain.service.FacturaCalculoResult;
import ec.sgi.backend.domain.service.FacturaTotalsCalculator;
import ec.sgi.backend.domain.service.ItemCalculo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PreordenService implements CrearPreordenUseCase, ListarPreordenesUseCase {
  private static final String ESTADO_CREADA = "CREADA";

  private final PreordenRepository preordenRepository;
  private final ClienteRepository clienteRepository;
  private final EmpresaRepository empresaRepository;
  private final ProductoRepository productoRepository;
  private final ImpuestoRepository impuestoRepository;
  private final InventarioRepository inventarioRepository;
  private final FacturaTotalsCalculator totalsCalculator;
  private final BodegaRepository bodegaRepository;

  public PreordenService(
      PreordenRepository preordenRepository,
      ClienteRepository clienteRepository,
      EmpresaRepository empresaRepository,
      ProductoRepository productoRepository,
      ImpuestoRepository impuestoRepository,
      InventarioRepository inventarioRepository,
      FacturaTotalsCalculator totalsCalculator,
      BodegaRepository bodegaRepository
  ) {
    this.preordenRepository = preordenRepository;
    this.clienteRepository = clienteRepository;
    this.empresaRepository = empresaRepository;
    this.productoRepository = productoRepository;
    this.impuestoRepository = impuestoRepository;
    this.inventarioRepository = inventarioRepository;
    this.totalsCalculator = totalsCalculator;
    this.bodegaRepository = bodegaRepository;
  }

  @Override
  public PreordenCreateResult crear(CrearPreordenCommand command) {
    clienteRepository.findByIdAndEmpresaId(command.clienteId(), command.empresaId())
        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    if (command.empresaId() == null) {
      throw new BusinessRuleException("Empresa requerida");
    }
    empresaRepository.findById(command.empresaId())
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

    List<ItemCalculo> itemsCalculo = new ArrayList<>();
    for (var item : command.items()) {
      if (item.bodegaId() != null) {
        bodegaRepository.findByIdAndEmpresaId(item.bodegaId(), command.empresaId())
            .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada"));
      }
      Producto producto = productoRepository.findByIdAndEmpresaId(item.productoId(), command.empresaId())
          .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
      Impuesto impuesto = impuestoRepository.findByIdAndEmpresaId(producto.impuestoId(), command.empresaId())
          .orElseThrow(() -> new ResourceNotFoundException("Impuesto no encontrado"));
      itemsCalculo.add(new ItemCalculo(
          item.bodegaId(),
          producto.id(),
          producto.codigo(),
          producto.descripcion(),
          item.cantidad(),
          producto.precioUnitario(),
          item.descuento(),
          impuesto.codigo(),
          impuesto.codigoPorcentaje(),
          impuesto.tarifa()
      ));
    }

    FacturaCalculoResult calculo = totalsCalculator.calcular(itemsCalculo);
    List<PreordenItem> preordenItems = calculo.items().stream()
        .map(item -> new PreordenItem(
            item.bodegaId(),
            item.productoId(),
            item.codigoPrincipal(),
            item.descripcion(),
            item.cantidad(),
            item.precioUnitario(),
            item.descuento(),
            item.precioTotalSinImpuesto()
        ))
        .toList();

    boolean reservaInventario = command.reservaInventario() != null && command.reservaInventario();
    Preorden preorden = new Preorden(
        null,
        command.empresaId(),
        command.clienteId(),
        LocalDateTime.now(),
        command.dirEstablecimiento(),
        command.moneda(),
        calculo.totales(),
        ESTADO_CREADA,
        command.observaciones(),
        reservaInventario,
        preordenItems
    );
    Preorden guardada = preordenRepository.save(preorden);

    if (reservaInventario) {
      reservarInventario(calculo.items(), command.empresaId());
    }

    return new PreordenCreateResult(guardada.id());
  }

  @Override
  public List<PreordenResult> listar(Long empresaId) {
    return preordenRepository.findByEmpresaId(empresaId).stream()
        .map(this::toResult)
        .toList();
  }

  private PreordenResult toResult(Preorden preorden) {
    FacturaTotales totales = preorden.totales();
    FacturaTotalesDto totalesDto = new FacturaTotalesDto(
        totales.totalSinImpuestos(),
        totales.totalDescuento(),
        totales.totalImpuestos(),
        totales.importeTotal()
    );

    List<PreordenItemResult> items = preorden.items().stream()
        .map(item -> new PreordenItemResult(
            item.bodegaId(),
            item.productoId(),
            item.codigoPrincipal(),
            item.descripcion(),
            item.cantidad(),
            item.precioUnitario(),
            item.descuento(),
            item.precioTotalSinImpuesto()
        ))
        .toList();

    return new PreordenResult(
        preorden.id(),
        preorden.empresaId(),
        preorden.clienteId(),
        preorden.fechaCreacion(),
        preorden.dirEstablecimiento(),
        preorden.moneda(),
        totalesDto,
        preorden.estado(),
        preorden.observaciones(),
        preorden.reservaInventario(),
        items
    );
  }

  private void reservarInventario(List<FacturaItem> items, Long empresaId) {
    LocalDateTime ahora = LocalDateTime.now();
    for (FacturaItem item : items) {
      if (item.bodegaId() != null) {
        Inventario inventario = inventarioRepository.findByProductoIdAndEmpresaIdAndBodegaIdForUpdate(
            item.productoId(),
            empresaId,
            item.bodegaId()
        ).orElseThrow(() -> new BusinessRuleException("Inventario no encontrado para producto " + item.productoId()));
        BigDecimal disponible = inventario.stockActual().subtract(inventario.stockReservado());
        if (disponible.compareTo(item.cantidad()) < 0) {
          throw new BusinessRuleException("Stock insuficiente para reservar producto " + item.productoId());
        }
        Inventario actualizado = inventario
            .withStockReservado(inventario.stockReservado().add(item.cantidad()))
            .withActualizadoEn(ahora);
        inventarioRepository.save(actualizado);
        continue;
      }
      List<Inventario> inventarios = inventarioRepository.findByProductoIdAndEmpresaId(
          item.productoId(),
          empresaId
      );
      if (inventarios.isEmpty()) {
        throw new BusinessRuleException("Inventario no encontrado para producto " + item.productoId());
      }
      BigDecimal disponibleTotal = inventarios.stream()
          .map(inv -> inv.stockActual().subtract(inv.stockReservado()))
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      if (disponibleTotal.compareTo(item.cantidad()) < 0) {
        throw new BusinessRuleException("Stock insuficiente para reservar producto " + item.productoId());
      }
      reservarPorBodega(inventarios, item.cantidad(), ahora);
    }
  }

  private void reservarPorBodega(List<Inventario> inventarios, BigDecimal cantidad, LocalDateTime ahora) {
    BigDecimal restante = cantidad;
    List<Inventario> ordenados = inventarios.stream()
        .sorted((a, b) -> {
          BigDecimal disponibleA = a.stockActual().subtract(a.stockReservado());
          BigDecimal disponibleB = b.stockActual().subtract(b.stockReservado());
          return disponibleB.compareTo(disponibleA);
        })
        .toList();
    for (Inventario inventario : ordenados) {
      if (restante.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
      BigDecimal disponible = inventario.stockActual().subtract(inventario.stockReservado());
      if (disponible.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      BigDecimal tomar = disponible.min(restante);
      Inventario actualizado = inventario
          .withStockReservado(inventario.stockReservado().add(tomar))
          .withActualizadoEn(ahora);
      inventarioRepository.save(actualizado);
      restante = restante.subtract(tomar);
    }
    if (restante.compareTo(BigDecimal.ZERO) > 0) {
      throw new BusinessRuleException("No se pudo reservar todo el stock disponible");
    }
  }
}
