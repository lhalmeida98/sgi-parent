package ec.sgi.backend.application.usecase;

import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.EmailAttachment;
import ec.sgi.backend.application.port.EmailService;
import ec.sgi.backend.application.port.in.GenerarFacturaPdfCommand;
import ec.sgi.backend.application.port.in.GenerarFacturaPdfUseCase;
import ec.sgi.backend.application.port.out.ClienteRepository;
import ec.sgi.backend.application.port.out.EmpresaRepository;
import ec.sgi.backend.application.port.out.FacturaRepository;
import ec.sgi.backend.domain.model.Cliente;
import ec.sgi.backend.domain.model.Empresa;
import ec.sgi.backend.domain.model.Factura;
import ec.sgi.backend.domain.model.InfoTributariaData;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import org.springframework.stereotype.Service;

@Service
public class EnviarFacturaPorEmailUseCase {
  private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private final EmailService emailService;
  private final GenerarFacturaPdfUseCase generarFacturaPdfUseCase;
  private final FacturaRepository facturaRepository;
  private final EmpresaRepository empresaRepository;
  private final ClienteRepository clienteRepository;

  public EnviarFacturaPorEmailUseCase(
      EmailService emailService,
      GenerarFacturaPdfUseCase generarFacturaPdfUseCase,
      FacturaRepository facturaRepository,
      EmpresaRepository empresaRepository,
      ClienteRepository clienteRepository
  ) {
    this.emailService = emailService;
    this.generarFacturaPdfUseCase = generarFacturaPdfUseCase;
    this.facturaRepository = facturaRepository;
    this.empresaRepository = empresaRepository;
    this.clienteRepository = clienteRepository;
  }

  public void execute(Long facturaId, Long empresaId, String subject) {
    if (facturaId == null) {
      throw new BusinessRuleException("Factura requerida");
    }
    Factura factura = facturaRepository.findById(facturaId)
        .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));
    if (!empresaId.equals(factura.empresaId())) {
      throw new BusinessRuleException("Factura no pertenece a la empresa");
    }
    Empresa empresa = empresaRepository.findById(empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
    Cliente cliente = clienteRepository.findById(factura.clienteId())
        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    String emailDestino = cliente.email();
    if (emailDestino == null || emailDestino.isBlank()) {
      throw new BusinessRuleException("Cliente sin email registrado");
    }
    String numeroFactura = buildNumeroFactura(factura.infoTributaria());
    String subjectFinal = subject == null || subject.isBlank()
        ? "Factura electronica SGI - " + numeroFactura
        : subject;
    String htmlBody = buildHtmlEmail(empresa, cliente, factura, numeroFactura);
    byte[] pdfBytes = generarFacturaPdfUseCase.generar(new GenerarFacturaPdfCommand(facturaId, empresaId));
    String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);
    String xml = decodeXmlStored(factura.xmlAutorizado());
    if (xml == null || xml.isBlank()) {
      throw new BusinessRuleException("XML no disponible para enviar");
    }
    String xmlBase64 = Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8));
    String baseName = buildAttachmentBaseName(numeroFactura, facturaId);
    List<EmailAttachment> attachments = List.of(
        new EmailAttachment(baseName + ".pdf", pdfBase64),
        new EmailAttachment(baseName + ".xml", xmlBase64)
    );
    emailService.enviarEmail(emailDestino, subjectFinal, htmlBody, attachments);
  }

  private String buildAttachmentBaseName(String numeroFactura, Long facturaId) {
    String base = numeroFactura == null ? "" : numeroFactura.trim();
    if (!base.isBlank()) {
      return "factura-" + sanitizeFilename(base);
    }
    return "factura-" + (facturaId == null ? "sin-numero" : facturaId);
  }

  private String sanitizeFilename(String value) {
    return value.replaceAll("[^A-Za-z0-9._-]", "_");
  }

  private String buildHtmlEmail(Empresa empresa, Cliente cliente, Factura factura, String numeroFactura) {
    String fecha = factura.fechaEmision() == null ? "" : FECHA_FORMAT.format(factura.fechaEmision());
    String total = formatMoney(factura.totales().importeTotal());
    String razonSocial = escapeHtml(cliente.razonSocial());
    String empresaNombre = escapeHtml(empresa.nombreComercial());
    String empresaRuc = escapeHtml(empresa.ruc());
    String direccion = escapeHtml(empresa.dirMatriz());

    return """
<!doctype html>
<html lang=\"es\">
  <head>
    <meta charset=\"utf-8\" />
    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />
    <title>Factura electronica</title>
  </head>
  <body style=\"margin:0;padding:0;background-color:#f4f6f8;font-family:Arial,Helvetica,sans-serif;\">\n"""
        + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background:#f4f6f8;padding:24px 0;\">\n"
        + "  <tr>\n"
        + "    <td align=\"center\">\n"
        + "      <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"640\" style=\"background:#ffffff;border-radius:10px;overflow:hidden;border:1px solid #e6e9ec;\">\n"
        + "        <tr>\n"
        + "          <td style=\"padding:24px 32px;background:#0b5fff;color:#ffffff;\">\n"
        + "            <div style=\"font-size:18px;font-weight:700;\">" + empresaNombre + "</div>\n"
        + "            <div style=\"font-size:12px;opacity:0.9;\">RUC: " + empresaRuc + "</div>\n"
        + "          </td>\n"
        + "        </tr>\n"
        + "        <tr>\n"
        + "          <td style=\"padding:28px 32px;\">\n"
        + "            <div style=\"font-size:18px;font-weight:700;color:#1f2a37;\">Factura electronica</div>\n"
        + "            <div style=\"margin-top:8px;font-size:14px;color:#4b5563;\">Hola " + razonSocial + ",</div>\n"
        + "            <div style=\"margin-top:8px;font-size:14px;color:#4b5563;\">Adjuntamos tu factura electronica. Si tienes dudas, responde a este correo.</div>\n"
        + "            <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"margin-top:20px;border-collapse:collapse;\">\n"
        + "              <tr>\n"
        + "                <td style=\"padding:12px;border:1px solid #e6e9ec;background:#f9fafb;font-size:13px;color:#6b7280;\">Numero</td>\n"
        + "                <td style=\"padding:12px;border:1px solid #e6e9ec;font-size:13px;color:#111827;font-weight:600;\">" + escapeHtml(numeroFactura) + "</td>\n"
        + "              </tr>\n"
        + "              <tr>\n"
        + "                <td style=\"padding:12px;border:1px solid #e6e9ec;background:#f9fafb;font-size:13px;color:#6b7280;\">Fecha de emision</td>\n"
        + "                <td style=\"padding:12px;border:1px solid #e6e9ec;font-size:13px;color:#111827;\">" + escapeHtml(fecha) + "</td>\n"
        + "              </tr>\n"
        + "              <tr>\n"
        + "                <td style=\"padding:12px;border:1px solid #e6e9ec;background:#f9fafb;font-size:13px;color:#6b7280;\">Total</td>\n"
        + "                <td style=\"padding:12px;border:1px solid #e6e9ec;font-size:13px;color:#111827;font-weight:600;\">" + total + "</td>\n"
        + "              </tr>\n"
        + "            </table>\n"
        + "            <div style=\"margin-top:18px;font-size:12px;color:#6b7280;\">Direccion matriz: " + direccion + "</div>\n"
        + "          </td>\n"
        + "        </tr>\n"
        + "        <tr>\n"
        + "          <td style=\"padding:16px 32px;background:#f9fafb;font-size:12px;color:#6b7280;\">\n"
        + "            Este correo fue generado automaticamente. Por favor no compartas tu factura con terceros.\n"
        + "          </td>\n"
        + "        </tr>\n"
        + "      </table>\n"
        + "    </td>\n"
        + "  </tr>\n"
        + "</table>\n"
        + "</body>\n"
        + "</html>";
  }

  private String buildNumeroFactura(InfoTributariaData info) {
    if (info == null) {
      return "";
    }
    String estab = info.estab() == null ? "" : info.estab();
    String ptoEmi = info.ptoEmi() == null ? "" : info.ptoEmi();
    String secuencial = info.secuencial() == null ? "" : info.secuencial();
    return estab + "-" + ptoEmi + "-" + secuencial;
  }

  private String formatMoney(BigDecimal valor) {
    if (valor == null) {
      return "0.00";
    }
    DecimalFormat formatter = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
    return formatter.format(valor.setScale(2, RoundingMode.HALF_UP));
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

  private String decodeXmlStored(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    if (!value.startsWith("gzip:")) {
      return value;
    }
    String base64 = value.substring("gzip:".length());
    try {
      byte[] compressed = Base64.getDecoder().decode(base64);
      try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
        return new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
      }
    } catch (IOException | IllegalArgumentException ex) {
      throw new BusinessRuleException("XML comprimido invalido");
    }
  }
}
