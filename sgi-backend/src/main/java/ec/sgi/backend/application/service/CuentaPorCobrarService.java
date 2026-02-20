package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.CuentaPorCobrarResult;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.ListarCuentasPorCobrarUseCase;
import ec.sgi.backend.application.port.out.ClienteRepository;
import ec.sgi.backend.application.port.out.CuentaPorCobrarRepository;
import ec.sgi.backend.application.port.out.DocumentoClienteRepository;
import ec.sgi.backend.domain.model.CuentaPorCobrar;
import ec.sgi.backend.domain.model.DocumentoCliente;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CuentaPorCobrarService implements ListarCuentasPorCobrarUseCase {
  private final CuentaPorCobrarRepository cuentaPorCobrarRepository;
  private final ClienteRepository clienteRepository;
  private final DocumentoClienteRepository documentoClienteRepository;

  public CuentaPorCobrarService(
      CuentaPorCobrarRepository cuentaPorCobrarRepository,
      ClienteRepository clienteRepository,
      DocumentoClienteRepository documentoClienteRepository
  ) {
    this.cuentaPorCobrarRepository = cuentaPorCobrarRepository;
    this.clienteRepository = clienteRepository;
    this.documentoClienteRepository = documentoClienteRepository;
  }

  @Override
  public List<CuentaPorCobrarResult> listar(Long empresaId, Long clienteId) {
    List<CuentaPorCobrar> cuentas;
    if (clienteId == null) {
      cuentas = cuentaPorCobrarRepository.findByEmpresaId(empresaId);
    } else {
      clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)
          .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
      cuentas = cuentaPorCobrarRepository.findByClienteIdAndEmpresaId(clienteId, empresaId);
    }
    return mapWithDocumentoNumero(empresaId, cuentas);
  }

  private List<CuentaPorCobrarResult> mapWithDocumentoNumero(Long empresaId, List<CuentaPorCobrar> cuentas) {
    if (cuentas == null || cuentas.isEmpty()) {
      return List.of();
    }
    List<Long> documentoIds = cuentas.stream()
        .map(CuentaPorCobrar::documentoClienteId)
        .filter(id -> id != null)
        .distinct()
        .toList();
    Map<Long, String> numeroPorDocumento = new HashMap<>();
    for (DocumentoCliente documento : documentoClienteRepository.findByIdInAndEmpresaId(documentoIds, empresaId)) {
      numeroPorDocumento.put(documento.id(), documento.numeroFactura());
    }
    LocalDate hoy = LocalDate.now();
    return cuentas.stream()
        .map(cuenta -> toResult(cuenta, numeroPorDocumento.get(cuenta.documentoClienteId()), hoy))
        .toList();
  }

  private CuentaPorCobrarResult toResult(CuentaPorCobrar cuenta, String numeroFactura, LocalDate hoy) {
    LocalDate vencimiento = cuenta.fechaVencimiento();
    Integer diasParaVencer = null;
    boolean vencida = false;
    String bucket = "SIN_VENCIMIENTO";
    if (vencimiento != null && hoy != null) {
      diasParaVencer = (int) ChronoUnit.DAYS.between(hoy, vencimiento);
      vencida = diasParaVencer < 0;
      bucket = resolveBucket(diasParaVencer);
    } else if (cuenta.creditoDias() != null && cuenta.creditoDias() > 0) {
      bucket = resolveCreditoBucket(cuenta.creditoDias());
    }
    String creditoBucket = (cuenta.creditoDias() == null || cuenta.creditoDias() <= 0)
        ? "SIN_CREDITO"
        : resolveCreditoBucket(cuenta.creditoDias());
    return new CuentaPorCobrarResult(
        cuenta.id(),
        cuenta.clienteId(),
        cuenta.documentoClienteId(),
        numeroFactura,
        cuenta.montoOriginal(),
        cuenta.montoCobrado(),
        cuenta.saldo(),
        cuenta.estado(),
        vencimiento,
        cuenta.creditoDias(),
        creditoBucket,
        diasParaVencer,
        vencida,
        bucket
    );
  }

  private String resolveBucket(int diasParaVencer) {
    if (diasParaVencer < 0) {
      return "VENCIDA";
    }
    if (diasParaVencer <= 7) {
      return "POR_VENCER_7";
    }
    if (diasParaVencer <= 15) {
      return "POR_VENCER_15";
    }
    if (diasParaVencer <= 30) {
      return "POR_VENCER_30";
    }
    return "FUTURA";
  }

  private String resolveCreditoBucket(int creditoDias) {
    if (creditoDias <= 30) {
      return "CREDITO_30";
    }
    if (creditoDias <= 60) {
      return "CREDITO_60";
    }
    if (creditoDias <= 90) {
      return "CREDITO_90";
    }
    return "CREDITO_MAS_90";
  }
}
