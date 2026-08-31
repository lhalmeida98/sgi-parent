package ec.sgi.backend.domain.model;

import java.util.Objects;

public record Empresa(
    Long id,
    String ambiente,
    String tipoEmision,
    String razonSocial,
    String nombreComercial,
    String ruc,
    String dirMatriz,
    String estab,
    String ptoEmi,
    String secuencial,
    String logoRuta,
    boolean obligadoContabilidad,
    RegimenTributario regimenTributario,
    boolean contribuyenteEspecial,
    String numeroContribuyenteEspecial,
    boolean agenteRetencion,
    Integer creditoDiasDefault
) {
  public Empresa {
    Objects.requireNonNull(ambiente, "ambiente");
    Objects.requireNonNull(tipoEmision, "tipoEmision");
    Objects.requireNonNull(razonSocial, "razonSocial");
    Objects.requireNonNull(nombreComercial, "nombreComercial");
    Objects.requireNonNull(ruc, "ruc");
    Objects.requireNonNull(dirMatriz, "dirMatriz");
    Objects.requireNonNull(estab, "estab");
    Objects.requireNonNull(ptoEmi, "ptoEmi");
    Objects.requireNonNull(secuencial, "secuencial");
    if (regimenTributario == null) {
      regimenTributario = RegimenTributario.GENERAL;
    }
  }

  public Empresa withSecuencial(String nuevoSecuencial) {
    return new Empresa(
        id,
        ambiente,
        tipoEmision,
        razonSocial,
        nombreComercial,
        ruc,
        dirMatriz,
        estab,
        ptoEmi,
        nuevoSecuencial,
        logoRuta,
        obligadoContabilidad,
        regimenTributario,
        contribuyenteEspecial,
        numeroContribuyenteEspecial,
        agenteRetencion,
        creditoDiasDefault
    );
  }

  public Empresa withLogoRuta(String nuevaRuta) {
    return new Empresa(
        id,
        ambiente,
        tipoEmision,
        razonSocial,
        nombreComercial,
        ruc,
        dirMatriz,
        estab,
        ptoEmi,
        secuencial,
        nuevaRuta,
        obligadoContabilidad,
        regimenTributario,
        contribuyenteEspecial,
        numeroContribuyenteEspecial,
        agenteRetencion,
        creditoDiasDefault
    );
  }

  public boolean regimenRimpe() {
    return regimenTributario != null && regimenTributario.esRimpe();
  }
}
