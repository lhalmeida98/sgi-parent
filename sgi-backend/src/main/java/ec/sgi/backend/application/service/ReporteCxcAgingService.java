package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.CxcAgingResumenResult;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.ReporteCxcAgingUseCase;
import ec.sgi.backend.application.port.out.ClienteRepository;
import ec.sgi.backend.application.port.out.CuentaPorCobrarRepository;
import ec.sgi.backend.domain.model.CuentaPorCobrar;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReporteCxcAgingService implements ReporteCxcAgingUseCase {
  private static final String ESTADO_COBRADA = "COBRADA";
  private static final String ESTADO_ANULADA = "ANULADA";

  private final CuentaPorCobrarRepository cuentaPorCobrarRepository;
  private final ClienteRepository clienteRepository;

  public ReporteCxcAgingService(
      CuentaPorCobrarRepository cuentaPorCobrarRepository,
      ClienteRepository clienteRepository
  ) {
    this.cuentaPorCobrarRepository = cuentaPorCobrarRepository;
    this.clienteRepository = clienteRepository;
  }

  @Override
  public CxcAgingResumenResult resumen(Long empresaId, Long clienteId) {
    List<CuentaPorCobrar> cuentas;
    if (clienteId == null) {
      cuentas = cuentaPorCobrarRepository.findByEmpresaId(empresaId);
    } else {
      clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)
          .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
      cuentas = cuentaPorCobrarRepository.findByClienteIdAndEmpresaId(clienteId, empresaId);
    }

    long total = 0;
    BigDecimal saldoTotal = BigDecimal.ZERO;

    long vencidas = 0;
    BigDecimal saldoVencido = BigDecimal.ZERO;

    long porVencer7 = 0;
    BigDecimal saldoPorVencer7 = BigDecimal.ZERO;

    long porVencer15 = 0;
    BigDecimal saldoPorVencer15 = BigDecimal.ZERO;

    long porVencer30 = 0;
    BigDecimal saldoPorVencer30 = BigDecimal.ZERO;

    long futuras = 0;
    BigDecimal saldoFuturo = BigDecimal.ZERO;

    LocalDate hoy = LocalDate.now();
    for (CuentaPorCobrar cuenta : cuentas) {
      if (ESTADO_COBRADA.equals(cuenta.estado()) || ESTADO_ANULADA.equals(cuenta.estado())) {
        continue;
      }
      BigDecimal saldo = cuenta.saldo();
      if (saldo == null || saldo.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      total++;
      saldoTotal = saldoTotal.add(saldo);
      LocalDate vencimiento = cuenta.fechaVencimiento();
      if (vencimiento == null) {
        futuras++;
        saldoFuturo = saldoFuturo.add(saldo);
        continue;
      }
      int dias = (int) ChronoUnit.DAYS.between(hoy, vencimiento);
      if (dias < 0) {
        vencidas++;
        saldoVencido = saldoVencido.add(saldo);
      } else if (dias <= 7) {
        porVencer7++;
        saldoPorVencer7 = saldoPorVencer7.add(saldo);
      } else if (dias <= 15) {
        porVencer15++;
        saldoPorVencer15 = saldoPorVencer15.add(saldo);
      } else if (dias <= 30) {
        porVencer30++;
        saldoPorVencer30 = saldoPorVencer30.add(saldo);
      } else {
        futuras++;
        saldoFuturo = saldoFuturo.add(saldo);
      }
    }

    return new CxcAgingResumenResult(
        total,
        saldoTotal,
        vencidas,
        saldoVencido,
        porVencer7,
        saldoPorVencer7,
        porVencer15,
        saldoPorVencer15,
        porVencer30,
        saldoPorVencer30,
        futuras,
        saldoFuturo
    );
  }
}
