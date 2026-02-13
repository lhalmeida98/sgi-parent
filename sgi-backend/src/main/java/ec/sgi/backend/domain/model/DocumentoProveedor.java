package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record DocumentoProveedor(
    Long id,
    Long empresaId,
    Long proveedorId,
    String tipoDocumento,
    String numeroDocumento,
    String numeroAutorizacion,
    LocalDate fechaEmision,
    LocalDate fechaVencimiento,
    BigDecimal subtotal,
    BigDecimal impuestos,
    BigDecimal total,
    String moneda,
    String estado,
    String xml,
    List<DocumentoProveedorItem> items,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
  public DocumentoProveedor {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(proveedorId, "proveedorId");
    Objects.requireNonNull(tipoDocumento, "tipoDocumento");
    Objects.requireNonNull(numeroDocumento, "numeroDocumento");
    Objects.requireNonNull(fechaEmision, "fechaEmision");
    Objects.requireNonNull(subtotal, "subtotal");
    Objects.requireNonNull(impuestos, "impuestos");
    Objects.requireNonNull(total, "total");
    Objects.requireNonNull(moneda, "moneda");
    Objects.requireNonNull(estado, "estado");
    items = items == null ? List.of() : List.copyOf(items);
  }
}
