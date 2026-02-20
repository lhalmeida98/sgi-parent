package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.DocumentoClienteRepository;
import ec.sgi.backend.domain.model.DocumentoCliente;
import ec.sgi.backend.infrastructure.persistence.entity.DocumentoClienteEntity;
import ec.sgi.backend.infrastructure.persistence.repository.DocumentoClienteJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DocumentoClienteRepositoryAdapter implements DocumentoClienteRepository {
  private final DocumentoClienteJpaRepository documentoClienteJpaRepository;

  public DocumentoClienteRepositoryAdapter(DocumentoClienteJpaRepository documentoClienteJpaRepository) {
    this.documentoClienteJpaRepository = documentoClienteJpaRepository;
  }

  @Override
  public DocumentoCliente save(DocumentoCliente documento) {
    return toDomain(documentoClienteJpaRepository.save(toEntity(documento)));
  }

  @Override
  public Optional<DocumentoCliente> findById(Long id) {
    return documentoClienteJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<DocumentoCliente> findByIdAndEmpresaId(Long id, Long empresaId) {
    return documentoClienteJpaRepository.findByIdAndEmpresaId(id, empresaId).map(this::toDomain);
  }

  @Override
  public Optional<DocumentoCliente> findByFacturaId(Long facturaId) {
    return documentoClienteJpaRepository.findByFacturaId(facturaId).map(this::toDomain);
  }

  @Override
  public List<DocumentoCliente> findByIdInAndEmpresaId(List<Long> ids, Long empresaId) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return documentoClienteJpaRepository.findByIdInAndEmpresaId(ids, empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<DocumentoCliente> findByEmpresaId(Long empresaId) {
    return documentoClienteJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<DocumentoCliente> findByClienteId(Long clienteId) {
    return documentoClienteJpaRepository.findByClienteId(clienteId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public boolean existsByEmpresaIdAndNumeroFactura(Long empresaId, String numeroFactura) {
    return documentoClienteJpaRepository.existsByEmpresaIdAndNumeroFactura(empresaId, numeroFactura);
  }

  private DocumentoCliente toDomain(DocumentoClienteEntity entity) {
    return new DocumentoCliente(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getClienteId(),
        entity.getFacturaId(),
        entity.getClaveAcceso(),
        entity.getNumeroFactura(),
        entity.getFechaEmision(),
        entity.getFechaVencimiento(),
        entity.getTotal(),
        entity.getEstado(),
        entity.getCreadoEn(),
        entity.getActualizadoEn()
    );
  }

  private DocumentoClienteEntity toEntity(DocumentoCliente documento) {
    DocumentoClienteEntity entity = new DocumentoClienteEntity();
    entity.setId(documento.id());
    entity.setEmpresaId(documento.empresaId());
    entity.setClienteId(documento.clienteId());
    entity.setFacturaId(documento.facturaId());
    entity.setClaveAcceso(documento.claveAcceso());
    entity.setNumeroFactura(documento.numeroFactura());
    entity.setFechaEmision(documento.fechaEmision());
    entity.setFechaVencimiento(documento.fechaVencimiento());
    entity.setTotal(documento.total());
    entity.setEstado(documento.estado());
    if (documento.creadoEn() == null) {
      entity.setCreadoEn(LocalDateTime.now());
    } else {
      entity.setCreadoEn(documento.creadoEn());
    }
    entity.setActualizadoEn(documento.actualizadoEn());
    return entity;
  }
}
