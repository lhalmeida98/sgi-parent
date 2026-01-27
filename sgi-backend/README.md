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
