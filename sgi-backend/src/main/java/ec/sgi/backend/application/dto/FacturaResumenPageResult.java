package ec.sgi.backend.application.dto;

import java.util.List;

public record FacturaResumenPageResult(
    List<FacturaResumenResult> items,
    int page,
    int size,
    long totalItems,
    int totalPages
) {
}
