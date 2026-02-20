package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.DocumentoClienteResult;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.port.in.AnularDocumentoClienteCommand;
import ec.sgi.backend.application.port.in.AnularDocumentoClienteUseCase;
import ec.sgi.backend.application.port.in.ListarDocumentosClienteUseCase;
import ec.sgi.backend.application.port.out.CuentaPorCobrarRepository;
import ec.sgi.backend.domain.model.CuentaPorCobrar;
import ec.sgi.backend.application.port.out.ClienteRepository;
import ec.sgi.backend.application.port.out.DocumentoClienteRepository;
import ec.sgi.backend.domain.model.DocumentoCliente;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DocumentoClienteService implements ListarDocumentosClienteUseCase, AnularDocumentoClienteUseCase {
  private static final String ESTADO_ANULADA = "ANULADA";
  private static final String ESTADO_COBRADA = "COBRADA";
  private static final String ESTADO_PARCIAL = "PARCIAL";
  private final DocumentoClienteRepository documentoClienteRepository;
  private final ClienteRepository clienteRepository;
  private final CuentaPorCobrarRepository cuentaPorCobrarRepository;

  public DocumentoClienteService(
      DocumentoClienteRepository documentoClienteRepository,
      ClienteRepository clienteRepository,
      CuentaPorCobrarRepository cuentaPorCobrarRepository
  ) {
    this.documentoClienteRepository = documentoClienteRepository;
    this.clienteRepository = clienteRepository;
    this.cuentaPorCobrarRepository = cuentaPorCobrarRepository;
  }

  @Override
  public List<DocumentoClienteResult> listar(Long empresaId, Long clienteId) {
    List<DocumentoCliente> documentos;
    if (clienteId == null) {
      documentos = documentoClienteRepository.findByEmpresaId(empresaId);
    } else {
      clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)
          .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
      documentos = documentoClienteRepository.findByClienteId(clienteId);
    }
    return documentos.stream().map(this::toResult).toList();
  }

  @Override
  public DocumentoClienteResult anular(AnularDocumentoClienteCommand command) {
    DocumentoCliente documento = documentoClienteRepository.findByIdAndEmpresaId(
            command.documentoClienteId(), command.empresaId())
        .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado"));
    if (ESTADO_ANULADA.equals(documento.estado())) {
      throw new BusinessRuleException("Documento ya esta anulado");
    }
    CuentaPorCobrar cuenta = cuentaPorCobrarRepository.findByDocumentoClienteId(documento.id()).orElse(null);
    if (cuenta != null) {
      if (ESTADO_PARCIAL.equals(cuenta.estado()) || ESTADO_COBRADA.equals(cuenta.estado())) {
        throw new BusinessRuleException("Documento con cobros aplicados, no se puede anular");
      }
      CuentaPorCobrar anulada = new CuentaPorCobrar(
          cuenta.id(),
          cuenta.empresaId(),
          cuenta.clienteId(),
          cuenta.documentoClienteId(),
          cuenta.montoOriginal(),
          cuenta.montoCobrado(),
          cuenta.saldo(),
          ESTADO_ANULADA,
          cuenta.fechaVencimiento(),
          cuenta.creditoDias(),
          cuenta.creadoEn(),
          LocalDateTime.now()
      );
      cuentaPorCobrarRepository.save(anulada);
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
        ESTADO_ANULADA,
        documento.creadoEn(),
        LocalDateTime.now()
    );
    DocumentoCliente guardado = documentoClienteRepository.save(actualizado);
    return toResult(guardado);
  }

  private DocumentoClienteResult toResult(DocumentoCliente documento) {
    LocalDate hoy = LocalDate.now();
    Integer diasParaVencer = null;
    boolean vencida = false;
    if (documento.fechaVencimiento() != null) {
      diasParaVencer = (int) ChronoUnit.DAYS.between(hoy, documento.fechaVencimiento());
      vencida = diasParaVencer < 0;
    }
    return new DocumentoClienteResult(
        documento.id(),
        documento.clienteId(),
        documento.facturaId(),
        documento.claveAcceso(),
        documento.numeroFactura(),
        documento.fechaEmision(),
        documento.fechaVencimiento(),
        documento.total(),
        documento.estado(),
        diasParaVencer,
        vencida
    );
  }
}
