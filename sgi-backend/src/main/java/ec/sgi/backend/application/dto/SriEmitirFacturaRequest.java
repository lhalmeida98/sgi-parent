package ec.sgi.backend.application.dto;

import java.util.List;

public record SriEmitirFacturaRequest(
    SriInfoTributariaDto infoTributaria,
    SriInfoFacturaDto infoFactura,
    List<SriDetalleDto> detalles,
    List<SriCampoAdicionalDto> infoAdicional,
    String codigoNumerico
) {
}
