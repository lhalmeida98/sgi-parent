package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.PagoProveedorResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.CrearPagoProveedorCommand;
import ec.sgi.backend.application.port.in.CrearPagoProveedorUseCase;
import ec.sgi.backend.application.port.in.ListarPagosProveedorUseCase;
import ec.sgi.backend.application.port.in.PagoProveedorDetalleCommand;
import ec.sgi.backend.application.port.out.CuentaPorPagarRepository;
import ec.sgi.backend.application.port.out.DocumentoProveedorRepository;
import ec.sgi.backend.application.port.out.PagoProveedorRepository;
import ec.sgi.backend.application.port.out.ProveedorRepository;
import ec.sgi.backend.domain.model.CuentaPorPagar;
import ec.sgi.backend.domain.model.DocumentoProveedor;
import ec.sgi.backend.domain.model.PagoProveedor;
import ec.sgi.backend.domain.model.PagoProveedorDetalle;
import ec.sgi.backend.domain.model.Proveedor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PagoProveedorService implements CrearPagoProveedorUseCase, ListarPagosProveedorUseCase {
  private static final String ESTADO_PENDIENTE = "PENDIENTE";
  private static final String ESTADO_PARCIAL = "PARCIAL";
  private static final String ESTADO_PAGADO = "PAGADO";
  private static final String ESTADO_REGISTRADO = "REGISTRADO";

  private final PagoProveedorRepository pagoProveedorRepository;
  private final ProveedorRepository proveedorRepository;
  private final CuentaPorPagarRepository cuentaPorPagarRepository;
  private final DocumentoProveedorRepository documentoProveedorRepository;

  public PagoProveedorService(
      PagoProveedorRepository pagoProveedorRepository,
      ProveedorRepository proveedorRepository,
      CuentaPorPagarRepository cuentaPorPagarRepository,
      DocumentoProveedorRepository documentoProveedorRepository
  ) {
    this.pagoProveedorRepository = pagoProveedorRepository;
    this.proveedorRepository = proveedorRepository;
    this.cuentaPorPagarRepository = cuentaPorPagarRepository;
    this.documentoProveedorRepository = documentoProveedorRepository;
  }

  @Override
  public PagoProveedorResult crear(CrearPagoProveedorCommand command) {
    Proveedor proveedor = proveedorRepository.findByIdAndEmpresaId(command.proveedorId(), command.empresaId())
        .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
    if (command.detalles() == null || command.detalles().isEmpty()) {
      throw new BusinessRuleException("Detalle de pago requerido");
    }
    BigDecimal totalAplicado = command.detalles().stream()
        .map(PagoProveedorDetalleCommand::montoAplicado)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (totalAplicado.compareTo(command.montoTotal()) != 0) {
      throw new BusinessRuleException("Monto total no coincide con el detalle aplicado");
    }

    List<PagoProveedorDetalle> detalles = new ArrayList<>();
    for (PagoProveedorDetalleCommand detalle : command.detalles()) {
      CuentaPorPagar cuenta = cuentaPorPagarRepository.findByIdAndEmpresaId(
              detalle.cuentaPorPagarId(), command.empresaId())
          .orElseThrow(() -> new ResourceNotFoundException("Cuenta por pagar no encontrada"));
      if (!cuenta.proveedorId().equals(proveedor.id())) {
        throw new BusinessRuleException("Cuenta por pagar no pertenece al proveedor");
      }
      if (cuenta.saldo().compareTo(detalle.montoAplicado()) < 0) {
        throw new BusinessRuleException("Monto aplicado supera el saldo de la cuenta");
      }
      CuentaPorPagar actualizada = actualizarCuenta(cuenta, detalle.montoAplicado());
      cuentaPorPagarRepository.save(actualizada);
      actualizarEstadoDocumento(actualizada.documentoProveedorId(), actualizada.estado());
      detalles.add(new PagoProveedorDetalle(null, cuenta.id(), detalle.montoAplicado()));
    }

    PagoProveedor pago = new PagoProveedor(
        null,
        command.empresaId(),
        proveedor.id(),
        command.fechaPago(),
        command.formaPago(),
        command.referencia(),
        command.montoTotal(),
        command.observacion(),
        detalles,
        null
    );
    PagoProveedor guardado = pagoProveedorRepository.save(pago);
    return toResult(guardado);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PagoProveedorResult> listar(Long empresaId, Long proveedorId) {
    List<PagoProveedor> pagos;
    if (proveedorId == null) {
      pagos = pagoProveedorRepository.findByEmpresaId(empresaId);
    } else {
      proveedorRepository.findByIdAndEmpresaId(proveedorId, empresaId)
          .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
      pagos = pagoProveedorRepository.findByProveedorId(proveedorId);
    }
    return pagos.stream().map(this::toResult).toList();
  }

  private CuentaPorPagar actualizarCuenta(CuentaPorPagar cuenta, BigDecimal montoAplicado) {
    BigDecimal nuevoPagado = cuenta.montoPagado().add(montoAplicado);
    BigDecimal nuevoSaldo = cuenta.montoOriginal().subtract(nuevoPagado);
    String estado;
    if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
      estado = ESTADO_PAGADO;
    } else if (nuevoSaldo.compareTo(cuenta.montoOriginal()) < 0) {
      estado = ESTADO_PARCIAL;
    } else {
      estado = ESTADO_PENDIENTE;
    }
    return new CuentaPorPagar(
        cuenta.id(),
        cuenta.empresaId(),
        cuenta.proveedorId(),
        cuenta.documentoProveedorId(),
        cuenta.montoOriginal(),
        nuevoPagado,
        nuevoSaldo,
        estado,
        cuenta.fechaVencimiento(),
        cuenta.creadoEn(),
        LocalDateTime.now()
    );
  }

  private void actualizarEstadoDocumento(Long documentoProveedorId, String estadoCxP) {
    if (documentoProveedorId == null) {
      return;
    }
    DocumentoProveedor documento = documentoProveedorRepository.findById(documentoProveedorId)
        .orElse(null);
    if (documento == null) {
      return;
    }
    String estado;
    if (ESTADO_PAGADO.equals(estadoCxP)) {
      estado = ESTADO_PAGADO;
    } else if (ESTADO_PARCIAL.equals(estadoCxP)) {
      estado = ESTADO_PARCIAL;
    } else {
      estado = ESTADO_REGISTRADO;
    }
    DocumentoProveedor actualizado = new DocumentoProveedor(
        documento.id(),
        documento.empresaId(),
        documento.proveedorId(),
        documento.tipoDocumento(),
        documento.numeroDocumento(),
        documento.numeroAutorizacion(),
        documento.fechaEmision(),
        documento.fechaVencimiento(),
        documento.subtotal(),
        documento.impuestos(),
        documento.total(),
        documento.moneda(),
        estado,
        documento.xml(),
        documento.items(),
        documento.creadoEn(),
        LocalDateTime.now()
    );
    documentoProveedorRepository.save(actualizado);
  }

  private PagoProveedorResult toResult(PagoProveedor pago) {
    return new PagoProveedorResult(
        pago.id(),
        pago.proveedorId(),
        pago.fechaPago(),
        pago.formaPago(),
        pago.referencia(),
        pago.montoTotal(),
        pago.observacion()
    );
  }
}
