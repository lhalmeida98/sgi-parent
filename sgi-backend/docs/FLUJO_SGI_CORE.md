# Flujo SGI -> Core SRI

1. SGI recibe la solicitud REST para crear factura.
2. SGI valida existencia de cliente y productos, calcula totales e impuestos.
3. SGI arma DTOs internos (SriInfoTributariaDto, SriInfoFacturaDto, SriDetalleDto).
4. Adaptador `SriCoreAdapter` transforma DTOs a comandos del core y ejecuta:
   - `EmitirComprobanteUseCase.emitir(...)` para registrar y enviar el comprobante.
   - `ConsultarComprobanteUseCase.consultar(...)` para actualizar el estado.
5. El core aplica la normativa (XML, XSD, firma, offline, estados, etc).
6. SGI persiste el estado local con la respuesta del core y expone el resultado.

Reglas:
- SGI orquesta y calcula reglas comerciales (clientes, productos, totales, impuestos).
- El core es la autoridad normativa; no se duplica logica SRI en SGI.
