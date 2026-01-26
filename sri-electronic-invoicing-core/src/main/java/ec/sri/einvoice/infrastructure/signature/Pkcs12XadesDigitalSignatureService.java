package ec.sri.einvoice.infrastructure.signature;

import ec.sri.einvoice.application.port.out.DigitalSignatureService;
import ec.sri.einvoice.domain.model.InfoTributaria;
import ec.sri.einvoice.infrastructure.config.AppProperties;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xml.security.Init;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import xades4j.providers.KeyingDataProvider;

import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.List;
import xades4j.algorithms.EnvelopedSignatureTransform;
import xades4j.providers.impl.FileSystemKeyStoreKeyingDataProvider;
import xades4j.providers.impl.KeyStoreKeyingDataProvider;
import xades4j.production.DataObjectReference;
import xades4j.production.SignedDataObjects;
import xades4j.production.XadesBesSigningProfile;
import xades4j.production.XadesSigner;

@Component
@ConditionalOnProperty(prefix = "app.signature", name = "type", havingValue = "pkcs12")
public class Pkcs12XadesDigitalSignatureService implements DigitalSignatureService {
  private final AppProperties properties;

  public Pkcs12XadesDigitalSignatureService(AppProperties properties) {
    this.properties = properties;
    Init.init();
  }

  @Override
  public String firmar(String xml, InfoTributaria infoTributaria) {
    SignatureMaterial material = resolveMaterial(infoTributaria);
    Document document = parseXml(xml);
    Element root = document.getDocumentElement();
    if (root == null) {
      throw new IllegalArgumentException("XML sin elemento raiz para firmar");
    }
    String id = root.getAttribute("id");
    if (isBlank(id)) {
      id = "comprobante";
      root.setAttribute("id", id);
    }
    root.setIdAttribute("id", true);

    try {
      KeyingDataProvider keyingDataProvider = buildKeyingProvider(material);
      XadesSigner signer = new XadesBesSigningProfile(keyingDataProvider).newSigner();
      DataObjectReference reference = (DataObjectReference) new DataObjectReference("#" + id)
          .withTransform(new EnvelopedSignatureTransform());
      SignedDataObjects dataObjects = new SignedDataObjects(reference);
      signer.sign(dataObjects, root);
      return toString(document);
    } catch (Exception ex) {
      throw new IllegalStateException(
          "No se pudo firmar el XML con XAdES usando el certificado " + material.p12Path(),
          ex
      );
    }
  }

  private SignatureMaterial resolveMaterial(InfoTributaria infoTributaria) {
    String p12Path = infoTributaria != null ? infoTributaria.firmaElectronica() : null;
    String password = infoTributaria != null ? infoTributaria.claveFirma() : null;
    AppProperties.Signature signature = properties.getSignature();
    if (isBlank(p12Path)) {
      p12Path = signature.getP12Path();
    }
    if (isBlank(password)) {
      password = signature.getP12Password();
    }
    if (isBlank(p12Path) || isBlank(password)) {
      throw new IllegalStateException("Falta configurar ruta o clave del certificado PKCS12");
    }
    return new SignatureMaterial(resolveP12Path(p12Path), password);
  }

  private KeyingDataProvider buildKeyingProvider(SignatureMaterial material) throws KeyStoreException {
    return new FileSystemKeyStoreKeyingDataProvider(
        "PKCS12",
        material.p12Path(),
        new FirstSigningCertSelector(),
        () -> material.password().toCharArray(),
        (alias, certificate) -> material.password().toCharArray(),
        false
    );
  }

  private String resolveP12Path(String p12Path) {
    String trimmed = p12Path.trim();
    if (trimmed.startsWith("classpath:")) {
      return copyClasspathP12(trimmed.substring("classpath:".length()));
    }
    Path path = Paths.get(trimmed);
    if (Files.exists(path)) {
      return path.toAbsolutePath().toString();
    }
    String classpathResolved = tryCopyClasspathP12(trimmed);
    if (classpathResolved != null) {
      return classpathResolved;
    }
    throw new IllegalStateException("No se encontro el archivo PKCS12 en la ruta: " + trimmed);
  }

  private String tryCopyClasspathP12(String resourcePath) {
    String normalized = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
    try (InputStream input = Pkcs12XadesDigitalSignatureService.class.getResourceAsStream(normalized)) {
      if (input == null) {
        return null;
      }
      return copyToTempFile(input);
    } catch (IOException ex) {
      throw new IllegalStateException("No se pudo cargar el PKCS12 desde classpath: " + resourcePath, ex);
    }
  }

  private String copyClasspathP12(String resourcePath) {
    String normalized = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
    try (InputStream input = Pkcs12XadesDigitalSignatureService.class.getResourceAsStream(normalized)) {
      if (input == null) {
        throw new IllegalStateException("No se encontro el PKCS12 en classpath: " + resourcePath);
      }
      return copyToTempFile(input);
    } catch (IOException ex) {
      throw new IllegalStateException("No se pudo cargar el PKCS12 desde classpath: " + resourcePath, ex);
    }
  }

  private String copyToTempFile(InputStream input) throws IOException {
    Path tempFile = Files.createTempFile("sri-p12-", ".p12");
    Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
    tempFile.toFile().deleteOnExit();
    return tempFile.toAbsolutePath().toString();
  }

  private Document parseXml(String xml) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      factory.setNamespaceAware(true);
      return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    } catch (Exception ex) {
      throw new IllegalArgumentException("XML invalido para firma", ex);
    }
  }

  private String toString(Document document) {
    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.INDENT, "no");
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(document), new StreamResult(writer));
      return writer.toString();
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudo serializar XML firmado", ex);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private static class FirstSigningCertSelector implements KeyStoreKeyingDataProvider.SigningCertSelector {
    @Override
    public X509Certificate selectCertificate(List<X509Certificate> availableCertificates) {
      if (availableCertificates == null || availableCertificates.isEmpty()) {
        throw new IllegalStateException("No se encontraron certificados en el PKCS12");
      }
      return availableCertificates.get(0);
    }
  }

  private record SignatureMaterial(String p12Path, String password) {
  }
}
