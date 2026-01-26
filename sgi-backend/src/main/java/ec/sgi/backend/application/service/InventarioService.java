package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.InventarioCreateResult;
import ec.sgi.backend.application.dto.InventarioResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.CrearInventarioCommand;
import ec.sgi.backend.application.port.in.CrearInventarioUseCase;
import ec.sgi.backend.application.port.in.ListarInventarioUseCase;
import ec.sgi.backend.application.port.out.InventarioRepository;
import ec.sgi.backend.application.port.out.ProductoRepository;
import ec.sgi.backend.domain.model.Inventario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventarioService implements CrearInventarioUseCase, ListarInventarioUseCase {
  private final InventarioRepository inventarioRepository;
  private final ProductoRepository productoRepository;

  public InventarioService(
      InventarioRepository inventarioRepository,
      ProductoRepository productoRepository
  ) {
    this.inventarioRepository = inventarioRepository;
    this.productoRepository = productoRepository;
  }

  @Override
  public InventarioCreateResult crear(CrearInventarioCommand command) {
    productoRepository.findByIdAndEmpresaId(command.productoId(), command.empresaId())
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

    boolean existe = inventarioRepository.findByProductoIdAndEmpresaIdForUpdate(
        command.productoId(),
        command.empresaId()
    ).isPresent();
    if (existe) {
      throw new BusinessRuleException("Inventario ya existe para el producto");
    }

    Inventario inventario = new Inventario(
        null,
        command.empresaId(),
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
  @Transactional(readOnly = true)
  public List<InventarioResult> listar(Long empresaId) {
    return inventarioRepository.findByEmpresaId(empresaId).stream()
        .map(this::toResult)
        .toList();
  }

  private InventarioResult toResult(Inventario inventario) {
    return new InventarioResult(
        inventario.id(),
        inventario.productoId(),
        inventario.stockActual(),
        inventario.stockReservado(),
        inventario.stockMinimo(),
        inventario.stockMaximo(),
        inventario.ubicacion(),
        inventario.costoPromedio(),
        inventario.actualizadoEn()
    );
  }
}
