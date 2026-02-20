package ec.sgi.backend.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record Usuario(
    Long id,
    Long empresaId,
    List<UsuarioEmpresa> empresas,
    String nombre,
    String usuario,
    String email,
    String passwordHash,
    List<String> roles,
    boolean activo,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {
  public Usuario {
    Objects.requireNonNull(empresaId, "empresaId");
    Objects.requireNonNull(empresas, "empresas");
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(usuario, "usuario");
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(passwordHash, "passwordHash");
    Objects.requireNonNull(roles, "roles");
  }
}
