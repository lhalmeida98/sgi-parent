package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.CuentaPorCobrarRepository;
import ec.sgi.backend.domain.model.CuentaPorCobrar;
import ec.sgi.backend.infrastructure.persistence.entity.CuentaPorCobrarEntity;
import ec.sgi.backend.infrastructure.persistence.repository.CuentaPorCobrarJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CuentaPorCobrarRepositoryAdapter implements CuentaPorCobrarRepository {
  private final CuentaPorCobrarJpaRepository cuentaPorCobrarJpaRepository;

  public CuentaPorCobrarRepositoryAdapter(CuentaPorCobrarJpaRepository cuentaPorCobrarJpaRepository) {
    this.cuentaPorCobrarJpaRepository = cuentaPorCobrarJpaRepository;
  }

  @Override
  public CuentaPorCobrar save(CuentaPorCobrar cuenta) {
    return toDomain(cuentaPorCobrarJpaRepository.save(toEntity(cuenta)));
  }

  @Override
  public Optional<CuentaPorCobrar> findById(Long id) {
    return cuentaPorCobrarJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<CuentaPorCobrar> findByIdAndEmpresaId(Long id, Long empresaId) {
    return cuentaPorCobrarJpaRepository.findByIdAndEmpresaId(id, empresaId).map(this::toDomain);
  }

  @Override
  public Optional<CuentaPorCobrar> findByDocumentoClienteId(Long documentoClienteId) {
    return cuentaPorCobrarJpaRepository.findByDocumentoClienteId(documentoClienteId).map(this::toDomain);
  }

  @Override
  public List<CuentaPorCobrar> findByEmpresaId(Long empresaId) {
    return cuentaPorCobrarJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<CuentaPorCobrar> findByClienteId(Long clienteId) {
    return cuentaPorCobrarJpaRepository.findByClienteId(clienteId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<CuentaPorCobrar> findByClienteIdAndEmpresaId(Long clienteId, Long empresaId) {
    return cuentaPorCobrarJpaRepository.findByClienteIdAndEmpresaId(clienteId, empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  private CuentaPorCobrar toDomain(CuentaPorCobrarEntity entity) {
    return new CuentaPorCobrar(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getClienteId(),
        entity.getDocumentoClienteId(),
        entity.getMontoOriginal(),
        entity.getMontoCobrado(),
        entity.getSaldo(),
        entity.getEstado(),
        entity.getFechaVencimiento(),
        entity.getCreditoDias(),
        entity.getCreadoEn(),
        entity.getActualizadoEn()
    );
  }

  private CuentaPorCobrarEntity toEntity(CuentaPorCobrar cuenta) {
    CuentaPorCobrarEntity entity = new CuentaPorCobrarEntity();
    entity.setId(cuenta.id());
    entity.setEmpresaId(cuenta.empresaId());
    entity.setClienteId(cuenta.clienteId());
    entity.setDocumentoClienteId(cuenta.documentoClienteId());
    entity.setMontoOriginal(cuenta.montoOriginal());
    entity.setMontoCobrado(cuenta.montoCobrado());
    entity.setSaldo(cuenta.saldo());
    entity.setEstado(cuenta.estado());
    entity.setFechaVencimiento(cuenta.fechaVencimiento());
    entity.setCreditoDias(cuenta.creditoDias());
    if (cuenta.creadoEn() == null) {
      entity.setCreadoEn(LocalDateTime.now());
    } else {
      entity.setCreadoEn(cuenta.creadoEn());
    }
    entity.setActualizadoEn(cuenta.actualizadoEn());
    return entity;
  }
}
