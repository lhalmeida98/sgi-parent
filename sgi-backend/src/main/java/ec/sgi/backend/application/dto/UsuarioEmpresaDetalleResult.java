package ec.sgi.backend.application.dto;

public record UsuarioEmpresaDetalleResult(
    EmpresaResult empresa,
    boolean principal
) {
}
