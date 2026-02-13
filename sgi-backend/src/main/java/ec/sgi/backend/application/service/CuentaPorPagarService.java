package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.CuentaPorPagarResult;
import ec.sgi.backend.application.port.in.ListarCuentasPorPagarUseCase;
import ec.sgi.backend.application.port.out.CuentaPorPagarRepository;
import ec.sgi.backend.application.port.out.DocumentoProveedorRepository;
import ec.sgi.backend.application.port.out.ProveedorRepository;
import ec.sgi.backend.domain.model.CuentaPorPagar;
import ec.sgi.backend.domain.model.DocumentoProveedor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CuentaPorPagarService implements ListarCuentasPorPagarUseCase {
  private final CuentaPorPagarRepository cuentaPorPagarRepository;
  private final ProveedorRepository proveedorRepository;
  private final DocumentoProveedorRepository documentoProveedorRepository;

  public CuentaPorPagarService(
      CuentaPorPagarRepository cuentaPorPagarRepository,
      ProveedorRepository proveedorRepository,
      DocumentoProveedorRepository documentoProveedorRepository
  ) {
    this.cuentaPorPagarRepository = cuentaPorPagarRepository;
    this.proveedorRepository = proveedorRepository;
    this.documentoProveedorRepository = documentoProveedorRepository;
  }

  @Override
  public List<CuentaPorPagarResult> listar(Long empresaId, Long proveedorId) {
    List<CuentaPorPagar> cuentas;
    if (proveedorId == null) {
      cuentas = cuentaPorPagarRepository.findByEmpresaId(empresaId);
    } else {
      proveedorRepository.findByIdAndEmpresaId(proveedorId, empresaId)
          .orElseThrow(() -> new ec.sgi.backend.application.exception.ResourceNotFoundException("Proveedor no encontrado"));
      cuentas = cuentaPorPagarRepository.findByProveedorIdAndEmpresaId(proveedorId, empresaId);
    }
    return mapWithDocumentoNumero(empresaId, cuentas);
  }

  private List<CuentaPorPagarResult> mapWithDocumentoNumero(Long empresaId, List<CuentaPorPagar> cuentas) {
    if (cuentas == null || cuentas.isEmpty()) {
      return List.of();
    }
    List<Long> documentoIds = cuentas.stream()
        .map(CuentaPorPagar::documentoProveedorId)
        .filter(id -> id != null)
        .distinct()
        .toList();
    Map<Long, String> numeroPorDocumento = new HashMap<>();
    Map<Long, String> tipoPorDocumento = new HashMap<>();
    for (DocumentoProveedor documento : documentoProveedorRepository.findByIdInAndEmpresaId(documentoIds, empresaId)) {
      numeroPorDocumento.put(documento.id(), documento.numeroDocumento());
      tipoPorDocumento.put(documento.id(), documento.tipoDocumento());
    }
    return cuentas.stream()
        .map(cuenta -> new CuentaPorPagarResult(
            cuenta.id(),
            cuenta.proveedorId(),
            cuenta.documentoProveedorId(),
            numeroPorDocumento.get(cuenta.documentoProveedorId()),
            tipoPorDocumento.get(cuenta.documentoProveedorId()),
            cuenta.montoOriginal(),
            cuenta.montoPagado(),
            cuenta.saldo(),
            cuenta.estado(),
            cuenta.fechaVencimiento()
        ))
        .toList();
  }
}
