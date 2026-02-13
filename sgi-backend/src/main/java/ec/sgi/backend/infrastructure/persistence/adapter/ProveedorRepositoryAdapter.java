package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.ProveedorRepository;
import ec.sgi.backend.domain.model.Proveedor;
import ec.sgi.backend.infrastructure.persistence.entity.ProveedorEntity;
import ec.sgi.backend.infrastructure.persistence.repository.ProveedorJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ProveedorRepositoryAdapter implements ProveedorRepository {
  private final ProveedorJpaRepository proveedorJpaRepository;

  public ProveedorRepositoryAdapter(ProveedorJpaRepository proveedorJpaRepository) {
    this.proveedorJpaRepository = proveedorJpaRepository;
  }

  @Override
  public Proveedor save(Proveedor proveedor) {
    return toDomain(proveedorJpaRepository.save(toEntity(proveedor)));
  }

  @Override
  public Optional<Proveedor> findById(Long id) {
    return proveedorJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<Proveedor> findByIdAndEmpresaId(Long id, Long empresaId) {
    return proveedorJpaRepository.findByIdAndEmpresaId(id, empresaId).map(this::toDomain);
  }

  @Override
  public List<Proveedor> findByEmpresaId(Long empresaId) {
    return proveedorJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public boolean existsByEmpresaIdAndIdentificacion(Long empresaId, String identificacion) {
    return proveedorJpaRepository.existsByEmpresaIdAndIdentificacion(empresaId, identificacion);
  }

  private Proveedor toDomain(ProveedorEntity entity) {
    return new Proveedor(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getTipoIdentificacion(),
        entity.getIdentificacion(),
        entity.getRazonSocial(),
        entity.getNombreComercial(),
        entity.getEmail(),
        entity.getTelefono(),
        entity.getDireccion(),
        entity.getCondicionesPago(),
        entity.getEstado(),
        entity.getCreadoEn(),
        entity.getActualizadoEn()
    );
  }

  private ProveedorEntity toEntity(Proveedor proveedor) {
    ProveedorEntity entity = new ProveedorEntity();
    entity.setId(proveedor.id());
    entity.setEmpresaId(proveedor.empresaId());
    entity.setTipoIdentificacion(proveedor.tipoIdentificacion());
    entity.setIdentificacion(proveedor.identificacion());
    entity.setRazonSocial(proveedor.razonSocial());
    entity.setNombreComercial(proveedor.nombreComercial());
    entity.setEmail(proveedor.email());
    entity.setTelefono(proveedor.telefono());
    entity.setDireccion(proveedor.direccion());
    entity.setCondicionesPago(proveedor.condicionesPago());
    entity.setEstado(proveedor.estado());
    if (proveedor.creadoEn() == null) {
      entity.setCreadoEn(LocalDateTime.now());
    } else {
      entity.setCreadoEn(proveedor.creadoEn());
    }
    entity.setActualizadoEn(proveedor.actualizadoEn());
    return entity;
  }
}
