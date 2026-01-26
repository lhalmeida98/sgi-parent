package ec.sri.einvoice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
  private Offline offline = new Offline();
  private Retry retry = new Retry();
  private Sri sri = new Sri();
  private Signature signature = new Signature();

  public Offline getOffline() {
    return offline;
  }

  public void setOffline(Offline offline) {
    this.offline = offline;
  }

  public Retry getRetry() {
    return retry;
  }

  public void setRetry(Retry retry) {
    this.retry = retry;
  }

  public Sri getSri() {
    return sri;
  }

  public void setSri(Sri sri) {
    this.sri = sri;
  }

  public Signature getSignature() {
    return signature;
  }

  public void setSignature(Signature signature) {
    this.signature = signature;
  }

  public static class Offline {
    private boolean enabled = false;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  public static class Retry {
    private int maxAttempts = 5;
    private long backoffSeconds = 30;
    private long schedulerMs = 60000;

    public int getMaxAttempts() {
      return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
      this.maxAttempts = maxAttempts;
    }

    public long getBackoffSeconds() {
      return backoffSeconds;
    }

    public void setBackoffSeconds(long backoffSeconds) {
      this.backoffSeconds = backoffSeconds;
    }

    public long getSchedulerMs() {
      return schedulerMs;
    }

    public void setSchedulerMs(long schedulerMs) {
      this.schedulerMs = schedulerMs;
    }
  }

  public static class Sri {
    private String client = "soap";
    private long timeoutSeconds = 30;
    private String xmlVersion = "1.0.0";
    private int autorizacionMaxIntentos = 6;
    private long autorizacionBackoffSeconds = 5;
    private long autorizacionBackoffMaxSeconds = 30;
    private Endpoints pruebas = new Endpoints();
    private Endpoints produccion = new Endpoints();

    public String getClient() {
      return client;
    }

    public void setClient(String client) {
      this.client = client;
    }

    public long getTimeoutSeconds() {
      return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
      this.timeoutSeconds = timeoutSeconds;
    }

    public String getXmlVersion() {
      return xmlVersion;
    }

    public void setXmlVersion(String xmlVersion) {
      this.xmlVersion = xmlVersion;
    }

    public int getAutorizacionMaxIntentos() {
      return autorizacionMaxIntentos;
    }

    public void setAutorizacionMaxIntentos(int autorizacionMaxIntentos) {
      this.autorizacionMaxIntentos = autorizacionMaxIntentos;
    }

    public long getAutorizacionBackoffSeconds() {
      return autorizacionBackoffSeconds;
    }

    public void setAutorizacionBackoffSeconds(long autorizacionBackoffSeconds) {
      this.autorizacionBackoffSeconds = autorizacionBackoffSeconds;
    }

    public long getAutorizacionBackoffMaxSeconds() {
      return autorizacionBackoffMaxSeconds;
    }

    public void setAutorizacionBackoffMaxSeconds(long autorizacionBackoffMaxSeconds) {
      this.autorizacionBackoffMaxSeconds = autorizacionBackoffMaxSeconds;
    }

    public Endpoints getPruebas() {
      return pruebas;
    }

    public void setPruebas(Endpoints pruebas) {
      this.pruebas = pruebas;
    }

    public Endpoints getProduccion() {
      return produccion;
    }

    public void setProduccion(Endpoints produccion) {
      this.produccion = produccion;
    }
  }

  public static class Endpoints {
    private String recepcionUrl;
    private String autorizacionUrl;
    private String consultaComprobanteUrl;
    private String consultaFacturaUrl;

    public String getRecepcionUrl() {
      return recepcionUrl;
    }

    public void setRecepcionUrl(String recepcionUrl) {
      this.recepcionUrl = recepcionUrl;
    }

    public String getAutorizacionUrl() {
      return autorizacionUrl;
    }

    public void setAutorizacionUrl(String autorizacionUrl) {
      this.autorizacionUrl = autorizacionUrl;
    }

    public String getConsultaComprobanteUrl() {
      return consultaComprobanteUrl;
    }

    public void setConsultaComprobanteUrl(String consultaComprobanteUrl) {
      this.consultaComprobanteUrl = consultaComprobanteUrl;
    }

    public String getConsultaFacturaUrl() {
      return consultaFacturaUrl;
    }

    public void setConsultaFacturaUrl(String consultaFacturaUrl) {
      this.consultaFacturaUrl = consultaFacturaUrl;
    }
  }

  public static class Signature {
    private String type = "dummy";
    private String p12Path;
    private String p12Password;
    private String keyAlias;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getP12Path() {
      return p12Path;
    }

    public void setP12Path(String p12Path) {
      this.p12Path = p12Path;
    }

    public String getP12Password() {
      return p12Password;
    }

    public void setP12Password(String p12Password) {
      this.p12Password = p12Password;
    }

    public String getKeyAlias() {
      return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
      this.keyAlias = keyAlias;
    }
  }
}
