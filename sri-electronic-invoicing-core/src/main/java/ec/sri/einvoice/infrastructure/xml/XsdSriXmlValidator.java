package ec.sri.einvoice.infrastructure.xml;

import ec.sri.einvoice.application.port.out.SriXmlValidator;
import ec.sri.einvoice.domain.model.TipoComprobante;
import java.io.StringReader;
import java.util.EnumMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

@Component
public class XsdSriXmlValidator implements SriXmlValidator {
  private final Map<TipoComprobante, Schema> schemas = new EnumMap<>(TipoComprobante.class);

  public XsdSriXmlValidator() {
    schemas.put(TipoComprobante.FACTURA, loadSchema("sri/xsd/factura.xsd"));
  }

  @Override
  public void validar(TipoComprobante tipoComprobante, String xml) {
    Schema schema = schemas.get(tipoComprobante);
    if (schema == null) {
      throw new IllegalArgumentException("No existe XSD para tipo: " + tipoComprobante);
    }
    try {
      Validator validator = schema.newValidator();
      validator.validate(new StreamSource(new StringReader(xml)));
    } catch (Exception ex) {
      throw new IllegalArgumentException("XML invalido para SRI: " + ex.getMessage(), ex);
    }
  }

  private Schema loadSchema(String classpathLocation) {
    try {
      var resource = getClass().getClassLoader().getResource(classpathLocation);
      if (resource == null) {
        throw new IllegalStateException("XSD no encontrado: " + classpathLocation);
      }
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      return factory.newSchema(resource);
    } catch (SAXException ex) {
      throw new IllegalStateException("No se pudo cargar XSD: " + classpathLocation, ex);
    }
  }
}
