package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.ClienteCreateResult;
import ec.sgi.backend.application.dto.ClienteResult;
import ec.sgi.backend.application.port.in.CrearClienteCommand;
import ec.sgi.backend.application.port.in.CrearClienteUseCase;
import ec.sgi.backend.application.port.in.ListarClientesUseCase;
import ec.sgi.backend.application.port.out.ClienteRepository;
import ec.sgi.backend.domain.model.Cliente;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClienteService implements CrearClienteUseCase, ListarClientesUseCase {
  private final ClienteRepository clienteRepository;

  public ClienteService(ClienteRepository clienteRepository) {
    this.clienteRepository = clienteRepository;
  }

  @Override
  public ClienteCreateResult crear(CrearClienteCommand command) {
    Cliente cliente = new Cliente(
        null,
        command.empresaId(),
        command.tipoIdentificacion(),
        command.identificacion(),
        command.razonSocial(),
        command.email(),
        command.direccion(),
        command.creditoDias()
    );
    Cliente guardado = clienteRepository.save(cliente);
    return new ClienteCreateResult(guardado.id());
  }

  @Override
  public List<ClienteResult> listar(Long empresaId) {
    return clienteRepository.findByEmpresaId(empresaId).stream()
        .map(this::toResult)
        .toList();
  }

  private ClienteResult toResult(Cliente cliente) {
    return new ClienteResult(
        cliente.id(),
        cliente.tipoIdentificacion(),
        cliente.identificacion(),
        cliente.razonSocial(),
        cliente.email(),
        cliente.direccion(),
        cliente.creditoDias()
    );
  }
}
