package ec.sgi.backend.application.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import ec.sgi.backend.application.exception.ForbiddenException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.GenerarFacturaPdfCommand;
import ec.sgi.backend.application.port.in.GenerarFacturaPdfUseCase;
import ec.sgi.backend.application.port.out.ClienteRepository;
import ec.sgi.backend.application.port.out.EmpresaRepository;
import ec.sgi.backend.application.port.out.FacturaRepository;
import ec.sgi.backend.domain.model.Cliente;
import ec.sgi.backend.domain.model.Empresa;
import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.FacturaItem;
import ec.sgi.backend.domain.model.FacturaPago;
import ec.sgi.backend.domain.model.InfoTributariaData;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FacturaPdfService implements GenerarFacturaPdfUseCase {
  private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter FECHA_HORA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
  private static final Pattern DIAS_CREDITO_PATTERN = Pattern.compile("(\\d{1,3})");
  private static final int DIAS_CREDITO_DEFAULT = 30;
  private final FacturaRepository facturaRepository;
  private final EmpresaRepository empresaRepository;
  private final ClienteRepository clienteRepository;

  public FacturaPdfService(
      FacturaRepository facturaRepository,
      EmpresaRepository empresaRepository,
      ClienteRepository clienteRepository
  ) {
    this.facturaRepository = facturaRepository;
    this.empresaRepository = empresaRepository;
    this.clienteRepository = clienteRepository;
  }

  @Override
  public byte[] generar(GenerarFacturaPdfCommand command) {
    Factura factura = facturaRepository.findById(command.facturaId())
        .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));
    if (!command.empresaId().equals(factura.empresaId())) {
      throw new ForbiddenException("Factura no pertenece a la empresa");
    }
    Empresa empresa = empresaRepository.findById(factura.empresaId())
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
    Cliente cliente = clienteRepository.findById(factura.clienteId())
        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

    String html = buildHtml(factura, empresa, cliente);
    return renderPdf(html);
  }

  private byte[] renderPdf(String html) {
    try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      PdfRendererBuilder builder = new PdfRendererBuilder();
      builder.withHtmlContent(html, null);
      builder.toStream(output);
      builder.run();
      return output.toByteArray();
    } catch (Exception ex) {
      throw new RuntimeException("No se pudo generar el PDF de factura", ex);
    }
  }

  String buildHtml(Factura factura, Empresa empresa, Cliente cliente) {
    InfoTributariaData info = factura.infoTributaria();
    String numeroFactura = safe(info.estab()) + "-" + safe(info.ptoEmi()) + "-" + safe(info.secuencial());
    String ambiente = mapAmbiente(info.ambiente());
    String emision = mapTipoEmision(info.tipoEmision());
    String fechaEmision = formatFecha(factura.fechaEmision());
    String fechaAutorizacion = formatFechaHora(factura.fechaAutorizacion());
    String claveAcceso = safe(factura.claveAcceso());
    String numeroAutorizacion = safe(factura.numeroAutorizacion());
    String dirEstablecimiento = resolveDirEstablecimiento(factura.dirEstablecimiento(), info.dirMatriz());

    String logoData = loadLogoDataUri(empresa.logoRuta());
    String barcodeData = claveAcceso.isBlank() ? "" : createBarcodeDataUri(claveAcceso);

    StringBuilder itemsRows = new StringBuilder();
    for (FacturaItem item : factura.items()) {
      itemsRows.append("<tr>")
          .append("<td>").append(escapeHtml(item.codigoPrincipal())).append("</td>")
          .append("<td>").append(escapeHtml(item.codigoPrincipal())).append("</td>")
          .append("<td class='text-right'>").append(formatCantidad(item.cantidad())).append("</td>")
          .append("<td>").append(escapeHtml(item.descripcion())).append("</td>")
          .append("<td></td>")
          .append("<td class='text-right'>").append(formatMoney(item.precioUnitario())).append("</td>")
          .append("<td class='text-right'>0.00</td>")
          .append("<td class='text-right'>0.00</td>")
          .append("<td class='text-right'>").append(formatMoney(item.descuento())).append("</td>")
          .append("<td class='text-right'>").append(formatMoney(item.precioTotalSinImpuesto())).append("</td>")
          .append("</tr>");
    }

    Integer creditoDiasRide = resolveCreditoDias(factura.pagos(), cliente);
    String pagosRows = buildPagoRow(factura.pagos(), factura.totales().importeTotal(), creditoDiasRide);
    String adicionalesRows = buildInfoAdicionalRows(
        cliente,
        factura,
        creditoDiasRide,
        factura.observaciones(),
        info.regimenTributario().leyendaSri()
    );

    StringBuilder html = new StringBuilder(4096);
    html.append("<?xml version='1.0' encoding='UTF-8'?>\n")
        .append("<html xmlns='http://www.w3.org/1999/xhtml'>\n")
        .append("<head>\n")
        .append("<meta charset='utf-8' />\n")
        .append("<style>\n")
        .append("  @page { size: A4; margin: 8mm; }\n")
        .append("  body { font-family: 'DejaVu Sans', Arial, sans-serif; font-size: 10px; color: #111; }\n")
        .append("  .row { width: 100%; }\n")
        .append("  .header-table { width: 100%; border-collapse: collapse; }\n")
        .append("  .header-table td { vertical-align: top; }\n")
        .append("  .left-panel { width: 64%; padding-right: 8px; }\n")
        .append("  .right-panel { width: 36%; }\n")
        .append("  .logo-box { height: 88px; }\n")
        .append("  .no-logo { color: #e00; font-size: 28px; font-weight: bold; letter-spacing: 3px; padding: 10px 0 0 20px; }\n")
        .append("  .factura-box { border: 1px solid #222; border-radius: 10px; padding: 8px; }\n")
        .append("  .factura-box h1 { margin: 4px 0; font-size: 20px; letter-spacing: 7px; font-weight: normal; }\n")
        .append("  .empresa-box { border: 1px solid #222; border-radius: 10px; padding: 8px; margin-top: 6px; min-height: 112px; }\n")
        .append("  .section-title { font-weight: bold; background: #f2f2f2; padding: 4px 6px; border: 1px solid #222; }\n")
        .append("  .info-table { width: 100%; border-collapse: collapse; margin-top: 6px; }\n")
        .append("  .info-table td { border: 1px solid #222; padding: 4px 6px; }\n")
        .append("  .items-table { width: 100%; border-collapse: collapse; margin-top: 6px; }\n")
        .append("  .items-table th, .items-table td { border: 1px solid #222; padding: 4px 5px; }\n")
        .append("  .items-table th { background: #f2f2f2; }\n")
        .append("  .totals-table { width: 100%; border-collapse: collapse; }\n")
        .append("  .totals-table td { border: 1px solid #222; padding: 3px 6px; }\n")
        .append("  .label { display: inline-block; min-width: 105px; }\n")
        .append("  .text-right { text-align: right; }\n")
        .append("  .barcode { margin-top: 6px; }\n")
        .append("  .mt-8 { margin-top: 8px; }\n")
        .append("  .mt-12 { margin-top: 12px; }\n")
        .append("  .two-col { width: 100%; border-collapse: collapse; margin-top: 12px; }\n")
        .append("  .two-col td { vertical-align: top; width: 50%; }\n")
        .append("</style>\n")
        .append("</head>\n")
        .append("<body>\n")
        .append("  <table class='header-table'>\n")
        .append("    <tr>\n")
        .append("      <td class='left-panel'>\n")
        .append("        <div class='logo-box'>\n")
        .append(logoData.isBlank() ? "          <div class='no-logo'>NO TIENE LOGO</div>\n"
            : "        <img src='" + logoData + "' style='max-height:90px; max-width:280px;' />\n")
        .append("        </div>\n")
        .append("        <div class='empresa-box'>\n")
        .append("          <div style='font-weight:bold; text-align:center;'>")
        .append(escapeHtml(info.razonSocial())).append("</div>\n")
        .append("          <div style='text-align:center;'>")
        .append(escapeHtml(info.nombreComercial())).append("</div>\n")
        .append("          <div style='margin-top:6px;'><strong>Direccion matriz:</strong> ")
        .append(escapeHtml(info.dirMatriz())).append("</div>\n")
        .append("          <div><strong>Direccion sucursal:</strong> ")
        .append(escapeHtml(dirEstablecimiento)).append("</div>\n");
    if (info.contribuyenteEspecial()) {
      html.append("          <div><strong>Contribuyente Especial Nro.:</strong> ")
          .append(escapeHtml(info.numeroContribuyenteEspecial())).append("</div>\n");
    }
    html.append("          <div><strong>OBLIGADO A LLEVAR CONTABILIDAD:</strong> ")
        .append(info.obligadoContabilidad() ? "SI" : "NO").append("</div>\n");
    String leyendaRimpe = info.regimenTributario().leyendaSri();
    if (leyendaRimpe != null) {
      html.append("          <div><strong>").append(escapeHtml(leyendaRimpe)).append("</strong></div>\n");
    }
    html.append("          <div><strong>RUC:</strong> ")
        .append(escapeHtml(info.ruc())).append("</div>\n")
        .append("        </div>\n")
        .append("      </td>\n")
        .append("      <td class='right-panel'>\n")
        .append("        <div class='factura-box'>\n")
        .append("        <div><strong>R.U.C.:</strong> ")
        .append(escapeHtml(info.ruc())).append("</div>\n")
        .append("        <h1>FACTURA</h1>\n")
        .append("        <div><strong>No.</strong> <span style='color:#c00; font-weight:bold;'>")
        .append(escapeHtml(numeroFactura)).append("</span></div>\n")
        .append("        <div style='margin-top:6px; font-weight:bold;'>NUMERO DE AUTORIZACION</div>\n")
        .append("        <div>").append(escapeHtml(numeroAutorizacion)).append("</div>\n")
        .append("        <div style='margin-top:6px;'><strong>FECHA Y HORA DE AUTORIZACION:</strong> ")
        .append(escapeHtml(fechaAutorizacion)).append("</div>\n")
        .append("        <div><strong>FECHA DE EMISION:</strong> ")
        .append(escapeHtml(fechaEmision)).append("</div>\n")
        .append("        <div><strong>AMBIENTE:</strong> ").append(escapeHtml(ambiente)).append("</div>\n")
        .append("        <div><strong>EMISION:</strong> ").append(escapeHtml(emision)).append("</div>\n")
        .append("        <div style='margin-top:6px; font-weight:bold; text-align:center;'>CLAVE DE ACCESO</div>\n");
    if (!barcodeData.isBlank()) {
      html.append("        <div class='barcode'><img src='").append(barcodeData)
          .append("' style='width:100%; height:70px;' /></div>\n");
    }
    html.append("        <div style='text-align:center;'>").append(escapeHtml(claveAcceso)).append("</div>\n")
        .append("        </div>\n")
        .append("      </td>\n")
        .append("    </tr>\n")
        .append("  </table>\n\n")
        .append("  <table class='info-table'>\n")
        .append("    <tr>\n")
        .append("      <td><strong>Razon Social / Nombres:</strong> ")
        .append(escapeHtml(cliente.razonSocial())).append("</td>\n")
        .append("      <td><strong>Identificacion:</strong> ")
        .append(escapeHtml(cliente.identificacion())).append("</td>\n")
        .append("    </tr>\n")
        .append("    <tr>\n")
        .append("      <td><strong>Fecha Emision:</strong> ").append(escapeHtml(fechaEmision)).append("</td>\n")
        .append("      <td><strong>Direccion:</strong> ").append(escapeHtml(cliente.direccion())).append("</td>\n")
        .append("    </tr>\n")
        .append("  </table>\n\n")
        .append("  <table class='items-table'>\n")
        .append("    <thead>\n")
        .append("      <tr>\n")
        .append("        <th>Codigo Principal</th>\n")
        .append("        <th>Cod. Auxiliar</th>\n")
        .append("        <th>Cantidad</th>\n")
        .append("        <th>Descripcion</th>\n")
        .append("        <th>Detalle Adicional</th>\n")
        .append("        <th>Precio unitario</th>\n")
        .append("        <th>Subsidio</th>\n")
        .append("        <th>Precio sin Subsidio</th>\n")
        .append("        <th>Descuento</th>\n")
        .append("        <th>Precio total</th>\n")
        .append("      </tr>\n")
        .append("    </thead>\n")
        .append("    <tbody>\n")
        .append(itemsRows)
        .append("    </tbody>\n")
        .append("  </table>\n\n")
        .append("  <table class='two-col'>\n")
        .append("    <tr>\n")
        .append("      <td>\n")
        .append("        <div class='section-title'>Informacion adicional</div>\n")
        .append("        <table class='info-table'>\n")
        .append(adicionalesRows)
        .append("        </table>\n")
        .append("        <div class='section-title mt-8'>Forma de pago</div>\n")
        .append("        <table class='items-table'>\n")
        .append("          <thead>\n")
        .append("            <tr>\n")
        .append("              <th>Forma de pago</th>\n")
        .append("              <th>Valor</th>\n")
        .append("            </tr>\n")
        .append("          </thead>\n")
        .append("          <tbody>\n")
        .append(pagosRows)
        .append("          </tbody>\n")
        .append("        </table>\n")
        .append("      </td>\n")
        .append("      <td>\n")
        .append("        <table class='totals-table'>\n")
        .append("          <tr>\n")
        .append("            <td>SUBTOTAL SIN IMPUESTOS</td>\n")
        .append("            <td class='text-right'>").append(formatMoney(factura.totales().totalSinImpuestos())).append("</td>\n")
        .append("          </tr>\n")
        .append("          <tr>\n")
        .append("            <td>DESCUENTO</td>\n")
        .append("            <td class='text-right'>").append(formatMoney(factura.totales().totalDescuento())).append("</td>\n")
        .append("          </tr>\n")
        .append("          <tr>\n")
        .append("            <td>IVA</td>\n")
        .append("            <td class='text-right'>").append(formatMoney(factura.totales().totalImpuestos())).append("</td>\n")
        .append("          </tr>\n")
        .append("          <tr>\n")
        .append("            <td><strong>VALOR TOTAL</strong></td>\n")
        .append("            <td class='text-right'><strong>")
        .append(formatMoney(factura.totales().importeTotal())).append("</strong></td>\n")
        .append("          </tr>\n")
        .append("        </table>\n")
        .append("      </td>\n")
        .append("    </tr>\n")
        .append("  </table>\n")
        .append("</body>\n")
        .append("</html>\n");
    return html.toString();
  }

  private String formatFecha(LocalDate fecha) {
    if (fecha == null) {
      return "";
    }
    return FECHA_FORMAT.format(fecha);
  }

  private String formatFechaHora(LocalDateTime fechaHora) {
    if (fechaHora != null) {
      return FECHA_HORA_FORMAT.format(fechaHora);
    }
    return "";
  }

  private String formatMoney(BigDecimal valor) {
    if (valor == null) {
      return "0.00";
    }
    DecimalFormat formatter = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
    return formatter.format(valor.setScale(2, RoundingMode.HALF_UP));
  }

  private String formatCantidad(BigDecimal valor) {
    if (valor == null) {
      return "0.00";
    }
    DecimalFormat formatter = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
    return formatter.format(valor.setScale(2, RoundingMode.HALF_UP));
  }

  private String mapAmbiente(String ambiente) {
    return switch (safe(ambiente)) {
      case "1" -> "PRUEBAS";
      case "2" -> "PRODUCCION";
      default -> safe(ambiente).isBlank() ? "" : safe(ambiente);
    };
  }

  private String mapTipoEmision(String tipoEmision) {
    return switch (safe(tipoEmision)) {
      case "1" -> "NORMAL";
      default -> safe(tipoEmision).isBlank() ? "" : safe(tipoEmision);
    };
  }

  private String loadLogoDataUri(String rutaLogo) {
    if (rutaLogo == null || rutaLogo.isBlank()) {
      return "";
    }
    try {
      Path path = Path.of(rutaLogo);
      if (!Files.exists(path)) {
        return "";
      }
      byte[] bytes = Files.readAllBytes(path);
      String extension = rutaLogo.toLowerCase(Locale.ROOT);
      String contentType = extension.endsWith(".png") ? "image/png" : "image/jpeg";
      return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
    } catch (Exception ex) {
      return "";
    }
  }

  private String createBarcodeDataUri(String data) {
    try {
      BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.CODE_128, 500, 90);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(matrix, "png", output);
      return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
    } catch (Exception ex) {
      return "";
    }
  }

  private String safe(String value) {
    return value == null ? "" : value;
  }

  private String escapeHtml(String value) {
    if (value == null) {
      return "";
    }
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  private String buildPagoRow(List<FacturaPago> pagos, BigDecimal totalFactura, Integer creditoDias) {
    if (pagos == null || pagos.isEmpty()) {
      if (creditoDias != null && creditoDias > 0) {
        return "<tr><td>OTROS CON UTILIZACION DEL SISTEMA FINANCIERO</td><td class='text-right'>"
            + formatMoney(totalFactura) + "</td></tr>";
      }
      return "<tr><td></td><td class='text-right'></td></tr>";
    }
    String formaPago = creditoDias != null && creditoDias > 0
        ? "OTROS CON UTILIZACION DEL SISTEMA FINANCIERO"
        : formaPagoRide(pagos.get(0).formaPago());
    BigDecimal suma = BigDecimal.ZERO;
    for (FacturaPago pago : pagos) {
      if (pago != null && pago.monto() != null) {
        suma = suma.add(pago.monto());
      }
    }
    if (suma.compareTo(BigDecimal.ZERO) == 0 && totalFactura != null) {
      suma = totalFactura;
    }
    return "<tr><td>" + escapeHtml(formaPago) + "</td><td class='text-right'>" + formatMoney(suma) + "</td></tr>";
  }

  private String buildInfoAdicionalRows(
      Cliente cliente,
      Factura factura,
      Integer creditoDias,
      String observaciones,
      String leyendaRimpe
  ) {
    StringBuilder rows = new StringBuilder();
    appendInfoAdicionalRow(rows, "Correo", cliente.email());
    if (creditoDias != null && creditoDias > 0) {
      appendInfoAdicionalRow(rows, "Forma de pago", "CREDITO " + creditoDias + " dias");
      appendInfoAdicionalRow(rows, "Credito", creditoDias + " dias");
      if (factura.fechaEmision() != null) {
        appendInfoAdicionalRow(rows, "Factura vence", formatFecha(factura.fechaEmision().plusDays(creditoDias)));
      }
    }
    appendInfoAdicionalRow(rows, "Observacion", observaciones);
    appendInfoAdicionalRow(rows, "Adicional", leyendaRimpe);
    if (rows.isEmpty()) {
      rows.append("<tr><td></td><td></td></tr>\n");
    }
    return rows.toString();
  }

  private void appendInfoAdicionalRow(StringBuilder rows, String label, String value) {
    if (value == null || value.isBlank()) {
      return;
    }
    rows.append("          <tr><td style='width:34%;'><strong>")
        .append(escapeHtml(label))
        .append(":</strong></td><td>")
        .append(escapeHtml(value.trim()))
        .append("</td></tr>\n");
  }

  private String formaPagoRide(String formaPago) {
    String normalizada = normalizeFormaPago(formaPago);
    if (normalizada.contains("CREDITO") || normalizada.contains("TRANSFERENCIA") || normalizada.contains("OTRO")) {
      return "OTROS CON UTILIZACION DEL SISTEMA FINANCIERO";
    }
    if (normalizada.contains("TARJETA")) {
      return "TARJETA DE CREDITO";
    }
    return "SIN UTILIZACION DEL SISTEMA FINANCIERO";
  }

  private Integer resolveCreditoDias(List<FacturaPago> pagos, Cliente cliente) {
    Integer max = null;
    if (pagos != null) {
      for (FacturaPago pago : pagos) {
        if (!normalizeFormaPago(pago.formaPago()).contains("CREDITO")) {
          continue;
        }
        Integer parsed = parseCreditoDias(pago.formaPago());
        int dias = sanitizeCreditoDias(parsed != null ? parsed : cliente.creditoDias());
        max = max == null ? dias : Math.max(max, dias);
      }
    }
    if (max != null) {
      return max;
    }
    if (pagos != null && !pagos.isEmpty()) {
      return null;
    }
    if (cliente.creditoDias() != null && cliente.creditoDias() > 0) {
      return sanitizeCreditoDias(cliente.creditoDias());
    }
    return null;
  }

  private Integer parseCreditoDias(String formaPago) {
    Matcher matcher = DIAS_CREDITO_PATTERN.matcher(formaPago == null ? "" : formaPago);
    if (!matcher.find()) {
      return null;
    }
    try {
      return Integer.parseInt(matcher.group(1));
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private int sanitizeCreditoDias(Integer dias) {
    if (dias == null || dias < 0) {
      return DIAS_CREDITO_DEFAULT;
    }
    return Math.min(dias, 365);
  }

  private String normalizeFormaPago(String formaPago) {
    if (formaPago == null) {
      return "";
    }
    String sinAcentos = Normalizer.normalize(formaPago, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "");
    return sinAcentos.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
  }

  private String resolveDirEstablecimiento(String dirEstablecimiento, String dirMatriz) {
    if (dirEstablecimiento != null && !dirEstablecimiento.isBlank()) {
      return dirEstablecimiento.trim();
    }
    if (dirMatriz != null && !dirMatriz.isBlank()) {
      return dirMatriz.trim();
    }
    return "";
  }
}
