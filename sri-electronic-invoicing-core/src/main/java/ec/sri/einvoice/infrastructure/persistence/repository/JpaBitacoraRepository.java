package ec.sri.einvoice.infrastructure.persistence.repository;

import ec.sri.einvoice.infrastructure.persistence.entity.BitacoraEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBitacoraRepository extends JpaRepository<BitacoraEntity, UUID> {
}
