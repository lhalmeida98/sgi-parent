package ec.sgi.backend.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PreordenResult(
    Long id,
    Long empresaId,
    Long clienteId,
    LocalDateTime fechaCreacion,
    String dirEstablecimiento,
    String moneda,
    FacturaTotalesDto totales,
    String estado,
    String observaciones,
    boolean reservaInventario,
    List<PreordenItemResult> items
) {
}
