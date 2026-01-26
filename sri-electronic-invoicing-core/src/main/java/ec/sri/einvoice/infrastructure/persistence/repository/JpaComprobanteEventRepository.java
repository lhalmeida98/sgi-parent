package ec.sri.einvoice.infrastructure.persistence.repository;

import ec.sri.einvoice.infrastructure.persistence.entity.ComprobanteEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaComprobanteEventRepository extends JpaRepository<ComprobanteEventEntity, UUID> {
}
