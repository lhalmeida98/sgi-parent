package ec.sgi.backend.domain.model;

import java.util.Objects;

public record UsuarioEmpresa(Long empresaId, boolean principal) {
  public UsuarioEmpresa {
    Objects.requireNonNull(empresaId, "empresaId");
  }
}
