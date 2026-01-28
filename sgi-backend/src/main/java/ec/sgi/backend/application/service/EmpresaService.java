package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.EmpresaCreateResult;
import ec.sgi.backend.application.dto.EmpresaResult;
import ec.sgi.backend.application.dto.FirmaElectronicaResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.CrearEmpresaCommand;
import ec.sgi.backend.application.port.in.ActualizarEmpresaCommand;
import ec.sgi.backend.application.port.in.ActualizarEmpresaUseCase;
import ec.sgi.backend.application.port.in.CrearEmpresaUseCase;
import ec.sgi.backend.application.port.in.ListarEmpresasUseCase;
import ec.sgi.backend.application.port.in.SubirFirmaElectronicaCommand;
import ec.sgi.backend.application.port.in.SubirFirmaElectronicaUseCase;
import ec.sgi.backend.application.port.in.SubirLogoEmpresaCommand;
import ec.sgi.backend.application.port.in.SubirLogoEmpresaUseCase;
import ec.sgi.backend.application.port.out.EmpresaRepository;
import ec.sgi.backend.application.port.out.FirmaElectronicaRepository;
import ec.sgi.backend.domain.model.Empresa;
import ec.sgi.backend.domain.model.FirmaElectronica;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmpresaService implements CrearEmpresaUseCase, ListarEmpresasUseCase,
    SubirFirmaElectronicaUseCase, ActualizarEmpresaUseCase, SubirLogoEmpresaUseCase {
  private static final String DEFAULT_TIPO_CONTENIDO = "application/x-pkcs12";
  private static final Set<String> LOGO_TIPOS_CONTENIDO = Set.of("image/png", "image/jpeg", "image/jpg");

  private final EmpresaRepository empresaRepository;
  private final FirmaElectronicaRepository firmaElectronicaRepository;
  private final Path storageDir;
  private final Path logoStorageDir;

  public EmpresaService(
      EmpresaRepository empresaRepository,
      FirmaElectronicaRepository firmaElectronicaRepository,
      @Value("${app.signature.storageDir:storage/firmas}") String storageDir,
      @Value("${app.empresa.logo.storageDir:storage/logos}") String logoStorageDir
  ) {
    this.empresaRepository = empresaRepository;
    this.firmaElectronicaRepository = firmaElectronicaRepository;
    this.storageDir = Path.of(storageDir);
    this.logoStorageDir = Path.of(logoStorageDir);
  }

  @Override
  public EmpresaCreateResult crear(CrearEmpresaCommand command) {
    validarSecuencial(command.secuencial());
    Empresa empresa = new Empresa(
        null,
        command.ambiente(),
        command.tipoEmision(),
        command.razonSocial(),
        command.nombreComercial(),
        command.ruc(),
        command.dirMatriz(),
        command.estab(),
        command.ptoEmi(),
        command.secuencial().trim(),
        null,
        command.obligadoContabilidad(),
        command.regimenRimpe()
    );
    Empresa guardada = empresaRepository.save(empresa);
    return new EmpresaCreateResult(guardada.id());
  }

  @Override
  public List<EmpresaResult> listar() {
    return empresaRepository.findAll().stream()
        .map(this::toResult)
        .toList();
  }

  @Override
  public EmpresaResult actualizar(Long empresaId, ActualizarEmpresaCommand command) {
    Empresa existente = empresaRepository.findById(empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
    validarSecuencial(command.secuencial());

    Empresa actualizada = new Empresa(
        existente.id(),
        command.ambiente(),
        command.tipoEmision(),
        command.razonSocial(),
        command.nombreComercial(),
        existente.ruc(),
        command.dirMatriz(),
        command.estab(),
        command.ptoEmi(),
        command.secuencial().trim(),
        existente.logoRuta(),
        command.obligadoContabilidad(),
        command.regimenRimpe()
    );
    Empresa guardada = empresaRepository.save(actualizada);
    return toResult(guardada);
  }

  @Override
  public FirmaElectronicaResult subir(SubirFirmaElectronicaCommand command) {
    Empresa empresa = empresaRepository.findById(command.empresaId())
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
    validarFirma(command);

    Optional<FirmaElectronica> existente = firmaElectronicaRepository.findByEmpresaId(empresa.id());
    Long firmaId = existente.map(FirmaElectronica::id).orElse(null);

    String tipoContenido = command.tipoContenido() == null || command.tipoContenido().isBlank()
        ? DEFAULT_TIPO_CONTENIDO
        : command.tipoContenido();
    Path rutaArchivo = guardarFirma(empresa.id(), command, existente);

    FirmaElectronica firma = new FirmaElectronica(
        firmaId,
        empresa.id(),
        command.nombreArchivo(),
        tipoContenido,
        rutaArchivo.toString(),
        command.clave()
    );
    FirmaElectronica guardada = firmaElectronicaRepository.save(firma);
    return new FirmaElectronicaResult(guardada.id(), guardada.nombreArchivo(), guardada.tipoContenido());
  }

  @Override
  public EmpresaResult subir(SubirLogoEmpresaCommand command) {
    Empresa empresa = empresaRepository.findById(command.empresaId())
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
    validarLogo(command);

    Path rutaArchivo = guardarLogo(empresa.id(), command, empresa.logoRuta());
    Empresa actualizada = empresa.withLogoRuta(rutaArchivo.toString());
    Empresa guardada = empresaRepository.save(actualizada);
    return toResult(guardada);
  }

  private EmpresaResult toResult(Empresa empresa) {
    return new EmpresaResult(
        empresa.id(),
        empresa.ambiente(),
        empresa.tipoEmision(),
        empresa.razonSocial(),
        empresa.nombreComercial(),
        empresa.ruc(),
        empresa.dirMatriz(),
        empresa.estab(),
        empresa.ptoEmi(),
        empresa.secuencial(),
        empresa.logoRuta(),
        empresa.obligadoContabilidad(),
        empresa.regimenRimpe()
    );
  }

  private void validarFirma(SubirFirmaElectronicaCommand command) {
    if (command.clave().isBlank()) {
      throw new BusinessRuleException("Clave de firma requerida");
    }
    if (command.contenido().length == 0) {
      throw new BusinessRuleException("Archivo de firma vacio");
    }
    String nombreArchivo = command.nombreArchivo();
    if (nombreArchivo.isBlank()) {
      throw new BusinessRuleException("Nombre de archivo requerido");
    }
    String lower = nombreArchivo.toLowerCase(Locale.ROOT);
    if (!lower.endsWith(".p12") && !lower.endsWith(".pfx")) {
      throw new BusinessRuleException("La firma debe ser un archivo .p12 o .pfx");
    }
  }

  private void validarLogo(SubirLogoEmpresaCommand command) {
    if (command.contenido().length == 0) {
      throw new BusinessRuleException("Archivo de logo vacio");
    }
    String nombreArchivo = command.nombreArchivo();
    if (nombreArchivo.isBlank()) {
      throw new BusinessRuleException("Nombre de archivo requerido");
    }
    String lower = nombreArchivo.toLowerCase(Locale.ROOT);
    boolean extensionValida = lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    if (!extensionValida) {
      throw new BusinessRuleException("El logo debe ser PNG o JPG");
    }
    String tipoContenido = command.tipoContenido();
    boolean tipoValido = tipoContenido == null || tipoContenido.isBlank()
        || "application/octet-stream".equalsIgnoreCase(tipoContenido)
        || LOGO_TIPOS_CONTENIDO.contains(tipoContenido);
    if (!tipoValido) {
      throw new BusinessRuleException("Tipo de contenido no soportado para logo");
    }
    if (!isSupportedImage(command.contenido())) {
      throw new BusinessRuleException("El logo no es una imagen valida");
    }
  }

  private boolean isSupportedImage(byte[] contenido) {
    if (contenido.length >= 8) {
      boolean isPng = (contenido[0] & 0xFF) == 0x89
          && (contenido[1] & 0xFF) == 0x50
          && (contenido[2] & 0xFF) == 0x4E
          && (contenido[3] & 0xFF) == 0x47
          && (contenido[4] & 0xFF) == 0x0D
          && (contenido[5] & 0xFF) == 0x0A
          && (contenido[6] & 0xFF) == 0x1A
          && (contenido[7] & 0xFF) == 0x0A;
      if (isPng) {
        return true;
      }
    }
    if (contenido.length >= 3) {
      return (contenido[0] & 0xFF) == 0xFF
          && (contenido[1] & 0xFF) == 0xD8
          && (contenido[2] & 0xFF) == 0xFF;
    }
    return false;
  }

  private Path guardarFirma(
      Long empresaId,
      SubirFirmaElectronicaCommand command,
      Optional<FirmaElectronica> existente
  ) {
    String nombreArchivo = command.nombreArchivo();
    String lower = nombreArchivo.toLowerCase(Locale.ROOT);
    String extension = lower.endsWith(".pfx") ? ".pfx" : ".p12";

    Path empresaDir = storageDir.resolve(empresaId.toString()).toAbsolutePath().normalize();
    try {
      Files.createDirectories(empresaDir);
    } catch (Exception ex) {
      throw new BusinessRuleException("No se pudo crear el directorio de firmas");
    }

    Path destino = empresaDir.resolve("firma-" + UUID.randomUUID() + extension);
    try {
      Files.write(destino, command.contenido(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    } catch (Exception ex) {
      throw new BusinessRuleException("No se pudo guardar la firma en disco");
    }

    existente.map(FirmaElectronica::rutaArchivo).ifPresent(this::eliminarArchivo);
    return destino;
  }

  private Path guardarLogo(Long empresaId, SubirLogoEmpresaCommand command, String rutaExistente) {
    String nombreArchivo = command.nombreArchivo();
    String lower = nombreArchivo.toLowerCase(Locale.ROOT);
    String extension = lower.endsWith(".png") ? ".png" : ".jpg";

    Path empresaDir = logoStorageDir.resolve(empresaId.toString()).toAbsolutePath().normalize();
    try {
      Files.createDirectories(empresaDir);
    } catch (Exception ex) {
      throw new BusinessRuleException("No se pudo crear el directorio de logos");
    }

    Path destino = empresaDir.resolve("logo-" + UUID.randomUUID() + extension);
    try {
      Files.write(destino, command.contenido(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    } catch (Exception ex) {
      throw new BusinessRuleException("No se pudo guardar el logo en disco");
    }

    eliminarArchivo(rutaExistente);
    return destino;
  }

  private void eliminarArchivo(String rutaArchivo) {
    if (rutaArchivo == null || rutaArchivo.isBlank()) {
      return;
    }
    try {
      Files.deleteIfExists(Path.of(rutaArchivo));
    } catch (Exception ex) {
    }
  }

  private void validarSecuencial(String secuencial) {
    String trimmed = secuencial == null ? "" : secuencial.trim();
    if (trimmed.isEmpty()) {
      throw new BusinessRuleException("Secuencial requerido");
    }
    for (int i = 0; i < trimmed.length(); i++) {
      char ch = trimmed.charAt(i);
      if (ch < '0' || ch > '9') {
        throw new BusinessRuleException("Secuencial debe ser numerico");
      }
    }
  }
}
