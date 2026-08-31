package ec.sgi.backend.application.exception;

import java.util.List;

public class ConfiguracionTributariaIncompletaException extends BusinessRuleException {
  public ConfiguracionTributariaIncompletaException(List<String> faltantes) {
    super("Configuracion tributaria incompleta: " + String.join(", ", faltantes));
  }
}
