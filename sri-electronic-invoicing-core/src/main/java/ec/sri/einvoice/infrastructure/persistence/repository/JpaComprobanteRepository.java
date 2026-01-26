package ec.sri.einvoice.infrastructure.persistence.repository;

import ec.sri.einvoice.infrastructure.persistence.entity.ComprobanteEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaComprobanteRepository extends JpaRepository<ComprobanteEntity, UUID> {
  List<ComprobanteEntity> findByEstadoInAndSiguienteReintentoLessThanEqual(List<String> estados, Instant ahora);
}
