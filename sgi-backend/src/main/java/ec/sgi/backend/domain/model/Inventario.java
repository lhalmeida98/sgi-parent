package ec.sgi.backend.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record Inventario(
    Long id,
    Long empresaId,
    Long productoId,
    BigDecimal stockActual,
    BigDecimal stockReservado,
    BigDecimal stockMinimo,
    BigDecimal stockMaximo,
    String ubicacion,
    BigDecimal costoPromedio,
    LocalDateTime actualizadoEn
) {
  public Inventario {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(productoId, "productoId");
    Objects.requireNonNull(stockActual, "stockActual");
    stockReservado = stockReservado == null ? BigDecimal.ZERO : stockReservado;
  }

  public Inventario withStockActual(BigDecimal nuevoStock) {
    return new Inventario(
        id,
        empresaId,
        productoId,
        nuevoStock,
        stockReservado,
        stockMinimo,
        stockMaximo,
        ubicacion,
        costoPromedio,
        actualizadoEn
    );
  }

  public Inventario withStockReservado(BigDecimal nuevoReservado) {
    return new Inventario(
        id,
        empresaId,
        productoId,
        stockActual,
        nuevoReservado,
        stockMinimo,
        stockMaximo,
        ubicacion,
        costoPromedio,
        actualizadoEn
    );
  }

  public Inventario withActualizadoEn(LocalDateTime nuevoTimestamp) {
    return new Inventario(
        id,
        empresaId,
        productoId,
        stockActual,
        stockReservado,
        stockMinimo,
        stockMaximo,
        ubicacion,
        costoPromedio,
        nuevoTimestamp
    );
  }
}
