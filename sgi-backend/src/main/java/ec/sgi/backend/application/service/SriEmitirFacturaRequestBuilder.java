package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.SriCampoAdicionalDto;
import ec.sgi.backend.application.dto.SriDetalleDto;
import ec.sgi.backend.application.dto.SriEmitirFacturaRequest;
import ec.sgi.backend.application.dto.SriImpuestoDto;
import ec.sgi.backend.application.dto.SriInfoFacturaDto;
import ec.sgi.backend.application.dto.SriInfoTributariaDto;
import ec.sgi.backend.application.dto.SriPagoDto;
import ec.sgi.backend.application.dto.SriTotalImpuestoDto;
import ec.sgi.backend.application.port.in.CrearFacturaCommand;
import ec.sgi.backend.application.port.in.PagoFacturaCommand;
import ec.sgi.backend.domain.model.Cliente;
import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.FacturaImpuesto;
import ec.sgi.backend.domain.model.FacturaItem;
import ec.sgi.backend.domain.service.FacturaCalculoResult;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SriEmitirFacturaRequestBuilder {
  private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final int DIAS_CREDITO_DEFAULT = 30;
  private static final int DIAS_CREDITO_MAX = 365;
  private static final Pattern DIAS_CREDITO_PATTERN = Pattern.compile("(\\d{1,3})");
  private static final String FORMA_PAGO_CREDITO = "CREDITO";
  private static final String SRI_SIN_SISTEMA_FINANCIERO = "01";
  private static final String SRI_TARJETA_CREDITO = "19";
  private static final String SRI_OTROS_SISTEMA_FINANCIERO = "20";

  private SriEmitirFacturaRequestBuilder() {
  }

  public static SriEmitirFacturaRequest build(
      CrearFacturaCommand command,
      Cliente cliente,
      FacturaCalculoResult calculo,
      Factura factura,
      BigDecimal propina,
      SriInfoTributariaDto infoTributaria
  ) {
    List<SriTotalImpuestoDto> totalImpuestos = calculo.impuestosTotales().stream()
        .map(total -> new SriTotalImpuestoDto(
            total.codigo(),
            total.codigoPorcentaje(),
            total.baseImponible(),
            total.valor()
        ))
        .toList();

    SriInfoFacturaDto infoFactura = new SriInfoFacturaDto(
        command.fechaEmision(),
        resolveDirEstablecimiento(factura.dirEstablecimiento(), infoTributaria.dirMatriz()),
        infoTributaria.contribuyenteEspecial(),
        infoTributaria.obligadoContabilidad(),
        cliente.tipoIdentificacion(),
        cliente.razonSocial(),
        cliente.identificacion(),
        truncateNullable(cliente.direccion(), 300),
        calculo.totales().totalSinImpuestos(),
        calculo.totales().totalDescuento(),
        propina,
        calculo.totales().importeTotal(),
        factura.moneda(),
        totalImpuestos
    );

    List<SriDetalleDto> detalles = calculo.items().stream()
        .map(SriEmitirFacturaRequestBuilder::toDetalle)
        .toList();

    return new SriEmitirFacturaRequest(
        infoTributaria,
        infoFactura,
        detalles,
        buildPagos(command.pagos(), cliente, calculo.totales().importeTotal()),
        buildInfoAdicional(
            cliente,
            factura.dirEstablecimiento(),
            infoTributaria.dirMatriz(),
            command.observaciones(),
            command.fechaEmision(),
            command.pagos()
        ),
        command.codigoNumerico()
    );
  }

  private static List<SriCampoAdicionalDto> buildInfoAdicional(
      Cliente cliente,
      String dirEstablecimiento,
      String dirMatriz,
      String observaciones,
      LocalDate fechaEmision,
      List<PagoFacturaCommand> pagos
  ) {
    List<SriCampoAdicionalDto> campos = new ArrayList<>();
    addCampo(campos, "Correo", cliente.email());
    Integer creditoDias = resolveCreditoDias(pagos, cliente);
    if (creditoDias != null && creditoDias > 0) {
      addCampo(campos, "Forma de pago", "CREDITO " + creditoDias + " dias");
      addCampo(campos, "Credito", creditoDias + " dias");
      if (fechaEmision != null) {
        addCampo(campos, "Factura vence", fechaEmision.plusDays(creditoDias).format(FECHA_FORMAT));
      }
    }
    addCampo(campos, "Observacion", observaciones);
    return campos;
  }

  private static List<SriPagoDto> buildPagos(List<PagoFacturaCommand> pagos, Cliente cliente, BigDecimal totalFactura) {
    if (pagos == null || pagos.isEmpty()) {
      if (cliente.creditoDias() != null && cliente.creditoDias() > 0) {
        int dias = sanitizeCreditoDias(cliente.creditoDias());
        return List.of(new SriPagoDto(SRI_OTROS_SISTEMA_FINANCIERO, totalFactura, dias, "dias"));
      }
      return List.of(new SriPagoDto(SRI_SIN_SISTEMA_FINANCIERO, totalFactura, null, null));
    }
    return pagos.stream()
        .map(pago -> toSriPago(pago, cliente))
        .toList();
  }

  private static SriPagoDto toSriPago(PagoFacturaCommand pago, Cliente cliente) {
    String formaPago = normalizeFormaPago(pago.formaPago());
    if (formaPago.contains(FORMA_PAGO_CREDITO)) {
      int dias = sanitizeCreditoDias(parseCreditoDias(pago.formaPago(), cliente.creditoDias()));
      return new SriPagoDto(SRI_OTROS_SISTEMA_FINANCIERO, pago.monto(), dias, "dias");
    }
    if (formaPago.contains("TARJETA")) {
      return new SriPagoDto(SRI_TARJETA_CREDITO, pago.monto(), null, null);
    }
    if (formaPago.contains("TRANSFERENCIA") || formaPago.contains("OTRO")) {
      return new SriPagoDto(SRI_OTROS_SISTEMA_FINANCIERO, pago.monto(), null, null);
    }
    return new SriPagoDto(SRI_SIN_SISTEMA_FINANCIERO, pago.monto(), null, null);
  }

  private static Integer resolveCreditoDias(List<PagoFacturaCommand> pagos, Cliente cliente) {
    if (pagos == null || pagos.isEmpty()) {
      return cliente.creditoDias() != null && cliente.creditoDias() > 0
          ? sanitizeCreditoDias(cliente.creditoDias())
          : null;
    }
    Integer max = null;
    for (PagoFacturaCommand pago : pagos) {
      if (!normalizeFormaPago(pago.formaPago()).contains(FORMA_PAGO_CREDITO)) {
        continue;
      }
      int dias = sanitizeCreditoDias(parseCreditoDias(pago.formaPago(), cliente.creditoDias()));
      max = max == null ? dias : Math.max(max, dias);
    }
    return max;
  }

  private static Integer parseCreditoDias(String formaPago, Integer clienteCreditoDias) {
    Matcher matcher = DIAS_CREDITO_PATTERN.matcher(formaPago == null ? "" : formaPago);
    if (matcher.find()) {
      try {
        return Integer.parseInt(matcher.group(1));
      } catch (NumberFormatException ignored) {
        return clienteCreditoDias;
      }
    }
    return clienteCreditoDias == null || clienteCreditoDias <= 0 ? DIAS_CREDITO_DEFAULT : clienteCreditoDias;
  }

  private static int sanitizeCreditoDias(Integer dias) {
    if (dias == null || dias < 0) {
      return DIAS_CREDITO_DEFAULT;
    }
    if (dias > DIAS_CREDITO_MAX) {
      return DIAS_CREDITO_MAX;
    }
    return dias;
  }

  private static String normalizeFormaPago(String formaPago) {
    if (formaPago == null) {
      return "";
    }
    String sinAcentos = Normalizer.normalize(formaPago, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "");
    return sinAcentos.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
  }

  private static void addCampo(List<SriCampoAdicionalDto> campos, String nombre, String valor) {
    String normalized = normalizeNullable(valor);
    if (normalized == null) {
      return;
    }
    campos.add(new SriCampoAdicionalDto(nombre, truncate(normalized, 300)));
  }

  private static String resolveDirEstablecimiento(String dirEstablecimiento, String dirMatriz) {
    String normalized = normalizeNullable(dirEstablecimiento);
    return normalized == null ? normalizeNullable(dirMatriz) : normalized;
  }

  private static String normalizeNullable(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private static String truncate(String value, int maxLength) {
    if (value.length() <= maxLength) {
      return value;
    }
    return value.substring(0, maxLength);
  }

  private static String truncateNullable(String value, int maxLength) {
    String normalized = normalizeNullable(value);
    return normalized == null ? null : truncate(normalized, maxLength);
  }

  private static SriDetalleDto toDetalle(FacturaItem item) {
    List<SriImpuestoDto> impuestos = item.impuestos().stream()
        .map(SriEmitirFacturaRequestBuilder::toImpuesto)
        .toList();
    return new SriDetalleDto(
        item.codigoPrincipal(),
        item.descripcion(),
        item.cantidad(),
        item.precioUnitario(),
        item.descuento(),
        item.precioTotalSinImpuesto(),
        impuestos
    );
  }

  private static SriImpuestoDto toImpuesto(FacturaImpuesto impuesto) {
    return new SriImpuestoDto(
        impuesto.codigo(),
        impuesto.codigoPorcentaje(),
        impuesto.tarifa(),
        impuesto.baseImponible(),
        impuesto.valor()
    );
  }
}
