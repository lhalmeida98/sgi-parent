package ec.sgi.backend.interfaces.rest;

import ec.sgi.backend.application.dto.EmpresaCreateRequest;
import ec.sgi.backend.application.dto.EmpresaCreateResult;
import ec.sgi.backend.application.dto.EmpresaResult;
import ec.sgi.backend.application.dto.EmpresaUpdateRequest;
import ec.sgi.backend.application.dto.FirmaElectronicaResult;
import ec.sgi.backend.application.exception.BusinessRuleException;
import ec.sgi.backend.application.port.in.ActualizarEmpresaCommand;
import ec.sgi.backend.application.port.in.ActualizarEmpresaUseCase;
import ec.sgi.backend.application.port.in.CrearEmpresaCommand;
import ec.sgi.backend.application.port.in.CrearEmpresaUseCase;
import ec.sgi.backend.application.port.in.ListarEmpresasUseCase;
import ec.sgi.backend.application.port.in.SubirFirmaElectronicaCommand;
import ec.sgi.backend.application.port.in.SubirFirmaElectronicaUseCase;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {
  private final CrearEmpresaUseCase crearEmpresaUseCase;
  private final ListarEmpresasUseCase listarEmpresasUseCase;
  private final SubirFirmaElectronicaUseCase subirFirmaElectronicaUseCase;
  private final ActualizarEmpresaUseCase actualizarEmpresaUseCase;

  public EmpresaController(
      CrearEmpresaUseCase crearEmpresaUseCase,
      ListarEmpresasUseCase listarEmpresasUseCase,
      SubirFirmaElectronicaUseCase subirFirmaElectronicaUseCase,
      ActualizarEmpresaUseCase actualizarEmpresaUseCase
  ) {
    this.crearEmpresaUseCase = crearEmpresaUseCase;
    this.listarEmpresasUseCase = listarEmpresasUseCase;
    this.subirFirmaElectronicaUseCase = subirFirmaElectronicaUseCase;
    this.actualizarEmpresaUseCase = actualizarEmpresaUseCase;
  }

  @PostMapping
  public ResponseEntity<EmpresaCreateResult> crear(@Valid @RequestBody EmpresaCreateRequest request) {
    EmpresaCreateResult result = crearEmpresaUseCase.crear(new CrearEmpresaCommand(
        request.ambiente(),
        request.tipoEmision(),
        request.razonSocial(),
        request.nombreComercial(),
        request.ruc(),
        request.dirMatriz(),
        request.estab(),
        request.ptoEmi(),
        request.secuencial()
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  public ResponseEntity<List<EmpresaResult>> listar() {
    return ResponseEntity.ok(listarEmpresasUseCase.listar());
  }

  @PutMapping("/{empresaId}")
  public ResponseEntity<EmpresaResult> actualizar(
      @PathVariable Long empresaId,
      @Valid @RequestBody EmpresaUpdateRequest request
  ) {
    EmpresaResult result = actualizarEmpresaUseCase.actualizar(empresaId, new ActualizarEmpresaCommand(
        request.ambiente(),
        request.tipoEmision(),
        request.razonSocial(),
        request.nombreComercial(),
        request.dirMatriz(),
        request.estab(),
        request.ptoEmi(),
        request.secuencial()
    ));
    return ResponseEntity.ok(result);
  }

  @PostMapping(path = "/{empresaId}/firma", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<FirmaElectronicaResult> subirFirma(
      @PathVariable Long empresaId,
      @RequestParam("archivo") MultipartFile archivo,
      @RequestParam("clave") String clave
  ) {
    String nombreArchivo = archivo.getOriginalFilename();
    if (nombreArchivo == null) {
      nombreArchivo = "";
    }
    byte[] contenido;
    try {
      contenido = archivo.getBytes();
    } catch (Exception ex) {
      throw new BusinessRuleException("No se pudo leer el archivo de firma");
    }
    FirmaElectronicaResult result = subirFirmaElectronicaUseCase.subir(new SubirFirmaElectronicaCommand(
        empresaId,
        nombreArchivo,
        archivo.getContentType(),
        contenido,
        clave
    ));
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }
}
