package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.ProveedorCreateResult;
import ec.sgi.backend.application.dto.ProveedorResult;
import ec.sgi.backend.application.dto.SriContribuyenteInfo;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.ActualizarProveedorCommand;
import ec.sgi.backend.application.port.in.ActualizarProveedorUseCase;
import ec.sgi.backend.application.port.in.CrearProveedorCommand;
import ec.sgi.backend.application.port.in.CrearProveedorUseCase;
import ec.sgi.backend.application.port.in.EliminarProveedorUseCase;
import ec.sgi.backend.application.port.in.ListarProveedoresUseCase;
import ec.sgi.backend.application.port.out.SriContribuyentePort;
import ec.sgi.backend.application.port.out.ProveedorRepository;
import ec.sgi.backend.domain.model.Proveedor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProveedorService implements CrearProveedorUseCase, ActualizarProveedorUseCase,
    ListarProveedoresUseCase, EliminarProveedorUseCase {
  private static final String ESTADO_ACTIVO = "ACTIVO";
  private static final String ESTADO_INACTIVO = "INACTIVO";

  private final ProveedorRepository proveedorRepository;
  private final SriContribuyentePort sriContribuyentePort;

  public ProveedorService(
      ProveedorRepository proveedorRepository,
      SriContribuyentePort sriContribuyentePort
  ) {
    this.proveedorRepository = proveedorRepository;
    this.sriContribuyentePort = sriContribuyentePort;
  }

  @Override
  public ProveedorCreateResult crear(CrearProveedorCommand command) {
    boolean existe = proveedorRepository.existsByEmpresaIdAndIdentificacion(
        command.empresaId(),
        command.identificacion()
    );
    if (existe) {
      throw new BusinessRuleException("Proveedor ya existe con esa identificacion");
    }
    SriContribuyenteInfo contribuyente = null;
    String avisoSri = null;
    if (esRuc(command.tipoIdentificacion())) {
      try {
        Optional<SriContribuyenteInfo> info = sriContribuyentePort.consultarPorRuc(command.identificacion());
        if (info.isEmpty()) {
          avisoSri = "RUC no encontrado en SRI, ingresar datos manualmente";
        } else {
          contribuyente = info.get();
          avisoSri = construirAvisoSri(command.razonSocial(), contribuyente);
        }
      } catch (RuntimeException ex) {
        avisoSri = "No se pudo consultar SRI, ingresar datos manualmente";
      }
    }
    String razonSocial = command.razonSocial();
    if (contribuyente != null && contribuyente.razonSocial() != null && !contribuyente.razonSocial().isBlank()) {
      razonSocial = contribuyente.razonSocial().trim();
    }
    String estado = Boolean.TRUE.equals(command.activo()) ? ESTADO_ACTIVO : ESTADO_INACTIVO;
    Proveedor proveedor = new Proveedor(
        null,
        command.empresaId(),
        command.tipoIdentificacion(),
        command.identificacion(),
        razonSocial,
        command.nombreComercial(),
        command.email(),
        command.telefono(),
        command.direccion(),
        command.condicionesPago(),
        estado,
        LocalDateTime.now(),
        null
    );
    Proveedor guardado = proveedorRepository.save(proveedor);
    return new ProveedorCreateResult(guardado.id(), avisoSri);
  }

  @Override
  public ProveedorResult actualizar(Long empresaId, Long proveedorId, ActualizarProveedorCommand command) {
    Proveedor existente = proveedorRepository.findByIdAndEmpresaId(proveedorId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
    String estado = command.activo() ? ESTADO_ACTIVO : ESTADO_INACTIVO;
    Proveedor actualizado = new Proveedor(
        existente.id(),
        existente.empresaId(),
        existente.tipoIdentificacion(),
        existente.identificacion(),
        command.razonSocial(),
        command.nombreComercial(),
        command.email(),
        command.telefono(),
        command.direccion(),
        command.condicionesPago(),
        estado,
        existente.creadoEn(),
        LocalDateTime.now()
    );
    Proveedor guardado = proveedorRepository.save(actualizado);
    return toResult(guardado);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProveedorResult> listar(Long empresaId) {
    return proveedorRepository.findByEmpresaId(empresaId).stream()
        .map(this::toResult)
        .toList();
  }

  @Override
  public void eliminar(Long empresaId, Long proveedorId) {
    Proveedor existente = proveedorRepository.findByIdAndEmpresaId(proveedorId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
    if (ESTADO_INACTIVO.equalsIgnoreCase(existente.estado())) {
      return;
    }
    Proveedor actualizado = new Proveedor(
        existente.id(),
        existente.empresaId(),
        existente.tipoIdentificacion(),
        existente.identificacion(),
        existente.razonSocial(),
        existente.nombreComercial(),
        existente.email(),
        existente.telefono(),
        existente.direccion(),
        existente.condicionesPago(),
        ESTADO_INACTIVO,
        existente.creadoEn(),
        LocalDateTime.now()
    );
    proveedorRepository.save(actualizado);
  }

  private ProveedorResult toResult(Proveedor proveedor) {
    return new ProveedorResult(
        proveedor.id(),
        proveedor.tipoIdentificacion(),
        proveedor.identificacion(),
        proveedor.razonSocial(),
        proveedor.nombreComercial(),
        proveedor.email(),
        proveedor.telefono(),
        proveedor.direccion(),
        proveedor.condicionesPago(),
        proveedor.estado()
    );
  }

  private boolean esRuc(String tipoIdentificacion) {
    if (tipoIdentificacion == null) {
      return false;
    }
    String tipo = tipoIdentificacion.trim().toUpperCase(Locale.ROOT);
    return tipo.contains("RUC");
  }

  private String construirAvisoSri(String razonSocialIngresada, SriContribuyenteInfo info) {
    StringBuilder aviso = new StringBuilder();
    if (info.estadoContribuyenteRuc() != null && !info.estadoContribuyenteRuc().isBlank()
        && !"ACTIVO".equalsIgnoreCase(info.estadoContribuyenteRuc())) {
      aviso.append("Contribuyente estado ").append(info.estadoContribuyenteRuc()).append(". ");
    }
    if (info.razonSocial() != null && razonSocialIngresada != null
        && !info.razonSocial().trim().equalsIgnoreCase(razonSocialIngresada.trim())) {
      aviso.append("Razon social actualizada desde SRI. ");
    }
    String texto = aviso.toString().trim();
    return texto.isBlank() ? null : texto;
  }
}
