package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.ClienteRepository;
import ec.sgi.backend.domain.model.Cliente;
import ec.sgi.backend.infrastructure.persistence.entity.ClienteEntity;
import ec.sgi.backend.infrastructure.persistence.repository.ClienteJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ClienteRepositoryAdapter implements ClienteRepository {
  private final ClienteJpaRepository clienteJpaRepository;

  public ClienteRepositoryAdapter(ClienteJpaRepository clienteJpaRepository) {
    this.clienteJpaRepository = clienteJpaRepository;
  }

  @Override
  public Cliente save(Cliente cliente) {
    return toDomain(clienteJpaRepository.save(toEntity(cliente)));
  }

  @Override
  public List<Cliente> findAll() {
    return clienteJpaRepository.findAll().stream().map(this::toDomain).toList();
  }

  @Override
  public Optional<Cliente> findById(Long id) {
    return clienteJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public List<Cliente> findByEmpresaId(Long empresaId) {
    return clienteJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Optional<Cliente> findByIdAndEmpresaId(Long id, Long empresaId) {
    return clienteJpaRepository.findByIdAndEmpresaId(id, empresaId).map(this::toDomain);
  }

  private Cliente toDomain(ClienteEntity entity) {
    return new Cliente(
        entity.getId(),
        entity.getEmpresaId(),
        entity.getTipoIdentificacion(),
        entity.getIdentificacion(),
        entity.getRazonSocial(),
        entity.getEmail(),
        entity.getDireccion()
    );
  }

  private ClienteEntity toEntity(Cliente cliente) {
    ClienteEntity entity = new ClienteEntity();
    entity.setId(cliente.id());
    entity.setEmpresaId(cliente.empresaId());
    entity.setTipoIdentificacion(cliente.tipoIdentificacion());
    entity.setIdentificacion(cliente.identificacion());
    entity.setRazonSocial(cliente.razonSocial());
    entity.setEmail(cliente.email());
    entity.setDireccion(cliente.direccion());
    return entity;
  }
}
