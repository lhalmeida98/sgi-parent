package ec.sgi.backend.application.mapper;

import ec.sgi.backend.application.dto.FacturaCreateRequest;
import ec.sgi.backend.application.dto.FacturaItemRequest;
import ec.sgi.backend.application.dto.FacturaPagoRequest;
import ec.sgi.backend.application.port.in.CrearFacturaCommand;
import ec.sgi.backend.application.port.in.ItemFacturaCommand;
import ec.sgi.backend.application.port.in.PagoFacturaCommand;
import java.util.List;

public class FacturaCommandMapper {
  public CrearFacturaCommand toCommand(FacturaCreateRequest request, Long empresaId) {
    return new CrearFacturaCommand(
        empresaId,
        request.clienteId(),
        request.preordenId(),
        request.dirEstablecimiento(),
        request.fechaEmision(),
        request.moneda(),
        request.codigoNumerico(),
        toItems(request.items()),
        toPagos(request.pagos())
    );
  }

  private List<ItemFacturaCommand> toItems(List<FacturaItemRequest> items) {
    return items.stream()
        .map(item -> new ItemFacturaCommand(item.productoId(), item.cantidad(), item.descuento()))
        .toList();
  }

  private List<PagoFacturaCommand> toPagos(List<FacturaPagoRequest> pagos) {
    return pagos.stream()
        .map(pago -> new PagoFacturaCommand(pago.formaPago(), pago.monto()))
        .toList();
  }
}
