package ec.sri.einvoice.infrastructure.persistence.adapter;

import ec.sri.einvoice.application.port.out.ComprobanteRepository;
import ec.sri.einvoice.domain.model.Comprobante;
import ec.sri.einvoice.domain.model.ComprobanteId;
import ec.sri.einvoice.domain.model.EstadoComprobante;
import ec.sri.einvoice.infrastructure.persistence.entity.ComprobanteEntity;
import ec.sri.einvoice.infrastructure.persistence.mapper.ComprobanteMapper;
import ec.sri.einvoice.infrastructure.persistence.repository.JpaComprobanteRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ComprobantePersistenceAdapter implements ComprobanteRepository {
  private final JpaComprobanteRepository repository;
  private final ComprobanteMapper mapper;

  public ComprobantePersistenceAdapter(JpaComprobanteRepository repository, ComprobanteMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Comprobante save(Comprobante comprobante) {
    ComprobanteEntity saved = repository.save(mapper.toEntity(comprobante));
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Comprobante> findById(ComprobanteId id) {
    return repository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  public List<Comprobante> findPendientes(Instant ahora) {
    List<String> estados = List.of(EstadoComprobante.EN_COLA.name(), EstadoComprobante.ERROR.name());
    return repository.findByEstadoInAndSiguienteReintentoLessThanEqual(estados, ahora)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }
}
