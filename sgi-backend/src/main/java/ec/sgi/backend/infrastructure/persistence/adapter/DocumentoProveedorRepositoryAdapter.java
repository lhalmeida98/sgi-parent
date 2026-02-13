package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.DocumentoProveedorRepository;
import ec.sgi.backend.domain.model.DocumentoProveedor;
import ec.sgi.backend.domain.model.DocumentoProveedorItem;
import ec.sgi.backend.infrastructure.persistence.entity.DocumentoProveedorEntity;
import ec.sgi.backend.infrastructure.persistence.entity.DocumentoProveedorItemEntity;
import ec.sgi.backend.infrastructure.persistence.repository.DocumentoProveedorJpaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DocumentoProveedorRepositoryAdapter implements DocumentoProveedorRepository {
  private final DocumentoProveedorJpaRepository documentoProveedorJpaRepository;

  public DocumentoProveedorRepositoryAdapter(DocumentoProveedorJpaRepository documentoProveedorJpaRepository) {
    this.documentoProveedorJpaRepository = documentoProveedorJpaRepository;
  }

  @Override
  public DocumentoProveedor save(DocumentoProveedor documento) {
    return toDomain(documentoProveedorJpaRepository.save(toEntity(documento)));
  }

  @Override
  public Optional<DocumentoProveedor> findById(Long id) {
    return documentoProveedorJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<DocumentoProveedor> findByIdAndEmpresaId(Long id, Long empresaId) {
    return documentoProveedorJpaRepository.findByIdAndEmpresaId(id, empresaId).map(this::toDomain);
  }

  @Override
  public List<DocumentoProveedor> findByIdInAndEmpresaId(List<Long> ids, Long empresaId) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return documentoProveedorJpaRepository.findByIdInAndEmpresaId(ids, empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<DocumentoProveedor> findByEmpresaId(Long empresaId) {
    return documentoProveedorJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<DocumentoProveedor> findByProveedorId(Long proveedorId) {
    return documentoProveedorJpaRepository.findByProveedorId(proveedorId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public boolean existsByEmpresaIdAndProveedorIdAndNumeroDocumento(Long empresaId, Long proveedorId,
      String numeroDocumento) {
    return documentoProveedorJpaRepository.existsByEmpresaIdAndProveedorIdAndNumeroDocumento(
        empresaId, proveedorId, numeroDocumento);
  }

  @Override
  public Optional<DocumentoProveedor> findByEmpresaIdAndNumeroAutorizacion(Long empresaId, String numeroAutorizacion) {
    return documentoProveedorJpaRepository.findByEmpresaIdAndNumeroAutorizacion(empresaId, numeroAutorizacion)
        .map(this::toDomain);
  }

  private DocumentoProveedor toDomain(DocumentoProveedorEntity entity) {
    List<DocumentoProveedorItem> items = entity.getItems().stream()
        .map(item -> new DocumentoProveedorItem(
            item.getId(),
            item.getBodegaId(),
            item.getProductoId(),
            item.getCodigoPrincipal(),
            item.getDescripcion(),
            item.getCantidad(),
            item.getCostoUnitario(),
            item.getSubtotal()
        ))
        .toList();
    return new DocumentoProveedor(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getProveedorId(),
        entity.getTipoDocumento(),
        entity.getNumeroDocumento(),
        entity.getNumeroAutorizacion(),
        entity.getFechaEmision(),
        entity.getFechaVencimiento(),
        entity.getSubtotal(),
        entity.getImpuestos(),
        entity.getTotal(),
        entity.getMoneda(),
        entity.getEstado(),
        entity.getXml(),
        items,
        entity.getCreadoEn(),
        entity.getActualizadoEn()
    );
  }

  private DocumentoProveedorEntity toEntity(DocumentoProveedor documento) {
    DocumentoProveedorEntity entity = new DocumentoProveedorEntity();
    entity.setId(documento.id());
    entity.setEmpresaId(documento.empresaId());
    entity.setProveedorId(documento.proveedorId());
    entity.setTipoDocumento(documento.tipoDocumento());
    entity.setNumeroDocumento(documento.numeroDocumento());
    entity.setNumeroAutorizacion(documento.numeroAutorizacion());
    entity.setFechaEmision(documento.fechaEmision());
    entity.setFechaVencimiento(documento.fechaVencimiento());
    entity.setSubtotal(documento.subtotal());
    entity.setImpuestos(documento.impuestos());
    entity.setTotal(documento.total());
    entity.setMoneda(documento.moneda());
    entity.setEstado(documento.estado());
    entity.setXml(documento.xml());
    if (documento.creadoEn() == null) {
      entity.setCreadoEn(LocalDateTime.now());
    } else {
      entity.setCreadoEn(documento.creadoEn());
    }
    entity.setActualizadoEn(documento.actualizadoEn());

    List<DocumentoProveedorItemEntity> items = new ArrayList<>();
    for (DocumentoProveedorItem item : documento.items()) {
      DocumentoProveedorItemEntity itemEntity = new DocumentoProveedorItemEntity();
      itemEntity.setId(item.id());
      itemEntity.setDocumentoProveedor(entity);
      itemEntity.setBodegaId(item.bodegaId());
      itemEntity.setProductoId(item.productoId());
      itemEntity.setCodigoPrincipal(item.codigoPrincipal());
      itemEntity.setDescripcion(item.descripcion());
      itemEntity.setCantidad(item.cantidad());
      itemEntity.setCostoUnitario(item.costoUnitario());
      itemEntity.setSubtotal(item.subtotal());
      items.add(itemEntity);
    }
    entity.setItems(items);

    return entity;
  }
}
