package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.CobroClienteResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.CobroClienteDetalleCommand;
import ec.sgi.backend.application.port.in.CrearCobroClienteCommand;
import ec.sgi.backend.application.port.in.CrearCobroClienteUseCase;
import ec.sgi.backend.application.port.in.ListarCobrosClienteUseCase;
import ec.sgi.backend.application.port.out.ClienteRepository;
import ec.sgi.backend.application.port.out.CobroClienteRepository;
import ec.sgi.backend.application.port.out.CuentaPorCobrarRepository;
import ec.sgi.backend.application.port.out.DocumentoClienteRepository;
import ec.sgi.backend.domain.model.CobroCliente;
import ec.sgi.backend.domain.model.CobroClienteDetalle;
import ec.sgi.backend.domain.model.CuentaPorCobrar;
import ec.sgi.backend.domain.model.DocumentoCliente;
import ec.sgi.backend.domain.model.Cliente;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CobroClienteService implements CrearCobroClienteUseCase, ListarCobrosClienteUseCase {
  private static final String ESTADO_PENDIENTE = "PENDIENTE";
  private static final String ESTADO_PARCIAL = "PARCIAL";
  private static final String ESTADO_COBRADA = "COBRADA";
  private static final String ESTADO_EMITIDA = "EMITIDA";
  private static final String ESTADO_ANULADA = "ANULADA";

  private final CobroClienteRepository cobroClienteRepository;
  private final ClienteRepository clienteRepository;
  private final CuentaPorCobrarRepository cuentaPorCobrarRepository;
  private final DocumentoClienteRepository documentoClienteRepository;

  public CobroClienteService(
      CobroClienteRepository cobroClienteRepository,
      ClienteRepository clienteRepository,
      CuentaPorCobrarRepository cuentaPorCobrarRepository,
      DocumentoClienteRepository documentoClienteRepository
  ) {
    this.cobroClienteRepository = cobroClienteRepository;
    this.clienteRepository = clienteRepository;
    this.cuentaPorCobrarRepository = cuentaPorCobrarRepository;
    this.documentoClienteRepository = documentoClienteRepository;
  }

  @Override
  public CobroClienteResult crear(CrearCobroClienteCommand command) {
    Cliente cliente = clienteRepository.findByIdAndEmpresaId(command.clienteId(), command.empresaId())
        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    if (command.detalles() == null || command.detalles().isEmpty()) {
      throw new BusinessRuleException("Detalle de cobro requerido");
    }
    BigDecimal totalAplicado = command.detalles().stream()
        .map(CobroClienteDetalleCommand::montoAplicado)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (totalAplicado.compareTo(command.montoTotal()) != 0) {
      throw new BusinessRuleException("Monto total no coincide con el detalle aplicado");
    }

    List<CobroClienteDetalle> detalles = new ArrayList<>();
    for (CobroClienteDetalleCommand detalle : command.detalles()) {
      CuentaPorCobrar cuenta = cuentaPorCobrarRepository.findByIdAndEmpresaId(
              detalle.cuentaPorCobrarId(), command.empresaId())
          .orElseThrow(() -> new ResourceNotFoundException("Cuenta por cobrar no encontrada"));
      if (!cuenta.clienteId().equals(cliente.id())) {
        throw new BusinessRuleException("Cuenta por cobrar no pertenece al cliente");
      }
      if (ESTADO_ANULADA.equals(cuenta.estado())) {
        throw new BusinessRuleException("Cuenta por cobrar anulada, no se puede aplicar cobros");
      }
      if (cuenta.saldo().compareTo(detalle.montoAplicado()) < 0) {
        throw new BusinessRuleException("Monto aplicado supera el saldo de la cuenta");
      }
      validarDocumentoActivo(command.empresaId(), cuenta.documentoClienteId());
      CuentaPorCobrar actualizada = actualizarCuenta(cuenta, detalle.montoAplicado());
      CuentaPorCobrar guardada = cuentaPorCobrarRepository.save(actualizada);
      actualizarEstadoDocumento(guardada.documentoClienteId(), guardada.estado(), guardada);
      detalles.add(new CobroClienteDetalle(null, cuenta.id(), detalle.montoAplicado()));
    }

    CobroCliente cobro = new CobroCliente(
        null,
        command.empresaId(),
        cliente.id(),
        command.fecha(),
        command.formaPago(),
        command.referencia(),
        command.montoTotal(),
        command.observacion(),
        detalles,
        null
    );
    CobroCliente guardado = cobroClienteRepository.save(cobro);
    return toResult(guardado);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CobroClienteResult> listar(Long empresaId, Long clienteId) {
    List<CobroCliente> cobros;
    if (clienteId == null) {
      cobros = cobroClienteRepository.findByEmpresaId(empresaId);
    } else {
      clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)
          .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
      cobros = cobroClienteRepository.findByClienteId(clienteId);
    }
    return cobros.stream().map(this::toResult).toList();
  }

  private CuentaPorCobrar actualizarCuenta(CuentaPorCobrar cuenta, BigDecimal montoAplicado) {
    BigDecimal nuevoCobrado = cuenta.montoCobrado().add(montoAplicado);
    BigDecimal nuevoSaldo = cuenta.montoOriginal().subtract(nuevoCobrado);
    String estado;
    if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
      estado = ESTADO_COBRADA;
    } else if (nuevoSaldo.compareTo(cuenta.montoOriginal()) < 0) {
      estado = ESTADO_PARCIAL;
    } else {
      estado = ESTADO_PENDIENTE;
    }
    return new CuentaPorCobrar(
        cuenta.id(),
        cuenta.empresaId(),
        cuenta.clienteId(),
        cuenta.documentoClienteId(),
        cuenta.montoOriginal(),
        nuevoCobrado,
        nuevoSaldo,
        estado,
        cuenta.fechaVencimiento(),
        cuenta.creditoDias(),
        cuenta.creadoEn(),
        LocalDateTime.now()
    );
  }

  private void validarDocumentoActivo(Long empresaId, Long documentoClienteId) {
    if (documentoClienteId == null) {
      return;
    }
    DocumentoCliente documento = documentoClienteRepository.findByIdAndEmpresaId(documentoClienteId, empresaId)
        .orElse(null);
    if (documento == null) {
      return;
    }
    if (ESTADO_ANULADA.equals(documento.estado())) {
      throw new BusinessRuleException("Documento anulado, no se puede aplicar cobros");
    }
  }

  private void actualizarEstadoDocumento(Long documentoClienteId, String estadoCxC, CuentaPorCobrar cuentaAnterior) {
    if (documentoClienteId == null) {
      return;
    }
    DocumentoCliente documento = documentoClienteRepository.findById(documentoClienteId).orElse(null);
    if (documento == null || ESTADO_ANULADA.equals(documento.estado())) {
      return;
    }
    String estado;
    if (ESTADO_COBRADA.equals(estadoCxC)) {
      estado = ESTADO_COBRADA;
    } else if (ESTADO_PARCIAL.equals(estadoCxC)) {
      estado = ESTADO_PARCIAL;
    } else {
      boolean esMixto = cuentaAnterior != null && cuentaAnterior.montoOriginal().compareTo(documento.total()) < 0;
      estado = esMixto ? ESTADO_PARCIAL : ESTADO_EMITIDA;
    }
    DocumentoCliente actualizado = new DocumentoCliente(
        documento.id(),
        documento.empresaId(),
        documento.clienteId(),
        documento.facturaId(),
        documento.claveAcceso(),
        documento.numeroFactura(),
        documento.fechaEmision(),
        documento.fechaVencimiento(),
        documento.total(),
        estado,
        documento.creadoEn(),
        LocalDateTime.now()
    );
    documentoClienteRepository.save(actualizado);
  }

  private CobroClienteResult toResult(CobroCliente cobro) {
    return new CobroClienteResult(
        cobro.id(),
        cobro.clienteId(),
        cobro.fecha(),
        cobro.formaPago(),
        cobro.referencia(),
        cobro.montoTotal(),
        cobro.observacion()
    );
  }
}
