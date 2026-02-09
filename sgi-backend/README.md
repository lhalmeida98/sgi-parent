# sgi-backend

Backend orquestador para SGI. Consume `sri-electronic-invoicing-core` y no duplica logica normativa.

## Requisitos
- Java 21
- Spring Boot 3.3.x
- PostgreSQL

## Nota Java 21 + firma XML
XAdES4j usa Guice 4.x que en Java 21 necesita abrir `java.lang` para generar bytecode. Si ves el error
`XadesProfileResolutionException: Unable to load cache item`, agrega este VM option en tu IDE:
```
--add-opens java.base/java.lang=ALL-UNNAMED
```
Si corres con Maven desde la raiz (`/home/henry/Documentos/Proyectos /sgi-parent`), ya esta configurado en `.mvn/jvm.config`.

## Estructura
```
src/main/java/ec/sgi/backend
  application
    dto
    mapper
    port
      in
      out
    service
  config
  domain
    model
    service
  infrastructure
    persistence
      adapter
      entity
      repository
    sri
  interface
    rest
```

## Endpoints
- `POST /api/facturas`
  - Crea una factura y la emite via core.
- `GET /api/facturas/{facturaId}/estado`
  - Consulta estado en SRI via core y actualiza el estado local.

## Swagger / OpenAPI
- UI: `GET /swagger-ui/index.html`
- JSON: `GET /v3/api-docs`

### Ejemplo request: crear factura
```json
{
  "clienteId": 1,
  "infoTributaria": {
    "ambiente": "PRUEBAS",
    "tipoEmision": "NORMAL",
    "razonSocial": "Mi Empresa",
    "nombreComercial": "Mi Empresa",
    "ruc": "9999999999999",
    "dirMatriz": "Av. Siempre Viva 123",
    "estab": "001",
    "ptoEmi": "002",
    "secuencial": "000000123"
  },
  "dirEstablecimiento": "Sucursal Centro",
  "fechaEmision": "2024-01-10",
  "moneda": "USD",
  "codigoNumerico": "12345678",
  "items": [
    {
      "productoId": 2,
      "cantidad": 2,
      "descuento": 0
    }
  ],
  "pagos": [
    {
      "formaPago": "EFECTIVO",
      "monto": 21.28
    }
  ]
}
```

## Integracion con sri-electronic-invoicing-core
- Inyeccion via Spring de `EmitirComprobanteUseCase` y `ConsultarComprobanteUseCase`.
- Adaptador `SriCoreAdapter` traduce DTOs SGI -> comandos del core.
- `SriCoreMapper` maneja conversion de enums (ambiente, tipo emision, tipo identificacion).

## Configuracion base
En `src/main/resources/application.yml` ajustar `spring.datasource.*` con credenciales reales.

## Correo (Resend)
Variables de entorno requeridas para enviar facturas por correo:
- `RESEND_API_KEY`
- `MAIL_FROM_EMAIL` (por defecto `onboarding@resend.dev`)
- `MAIL_FROM_NAME` (opcional)

El arranque valida que exista `RESEND_API_KEY` (fail-fast). Si `MAIL_FROM_NAME` no existe,
se enviara sin nombre visible.

El envio de facturas adjunta ambos archivos:
- `factura.pdf`
- `factura.xml`

Si usas el remitente `@resend.dev`, Resend limita los envios a tu email verificado (modo prueba).

### Envio automatico al autorizar
Por defecto el sistema envia la factura automaticamente cuando pasa a estado `AUTORIZADA`.
Puedes desactivar con:
- `app.facturas.email.auto-send-authorized=false`

Para consultar automaticamente el estado con SRI, el scheduler revisa facturas en proceso cada:
- `app.facturas.scheduler.fixed-delay-ms` (por defecto 15000 ms)

Al crear una factura, se hace una consulta inmediata al SRI despues de un delay:
- `app.facturas.sri.consulta-inmediata.enabled=true`
- `app.facturas.sri.consulta-inmediata.delay-ms=10000`

### XML firmado
Se guarda el XML firmado (por defecto comprimido con gzip + base64) en `facturas.xml_firmado`.
Configurable con:
- `app.facturas.xml-firmado.store=true`
- `app.facturas.xml-firmado.compress=true`

Al autorizar, el XML firmado pasa a `xml_autorizado` (tambien comprimido) y se limpia `xml_firmado`.
Configurable con:
- `app.facturas.xml-autorizado.compress=true`

### Reenvio puntual
Endpoint para consultar el estado de una factura especifica en proceso:
- `POST /api/facturas/{facturaId}/reenviar`

### Health mail
Endpoint publico para verificar la configuracion:
- `GET /api/email/config`
Respuesta esperada:
```json
{
  "status": "ok",
  "provider": "resend",
  "fromEmail": "onboarding@resend.dev",
  "fromName": "",
  "restrictedToAccountEmail": true
}
```
