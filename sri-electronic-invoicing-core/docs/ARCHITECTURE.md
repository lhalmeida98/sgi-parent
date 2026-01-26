# Arquitectura

## Capas

- domain
  - Modela entidades, value objects, estados y eventos.
  - Contiene servicios de dominio (validacion, transiciones, clave de acceso).
  - No depende de Spring ni de infraestructura.
- application
  - Casos de uso (emitir comprobante, reintentar envio).
  - Define puertos para firmas, persistencia, XML y SRI.
  - Orquesta el dominio y la infraestructura mediante interfaces.
- infrastructure
  - Adaptadores Spring (JPA, XML, XSD, firma dummy, SRI dummy, scheduler).
  - Configuracion de beans y propiedades.

## Flujo principal

1. EmitirComprobanteService crea el comprobante y genera clave de acceso.
2. Valida reglas de negocio y genera XML.
3. Valida XML contra XSD y firma digital.
4. Encola para envio offline o envia si el modo offline esta deshabilitado.
5. ReintentarEnvioService procesa pendientes con politica de reintentos.

## Paquetes clave

- ec.sri.einvoice.domain.model
- ec.sri.einvoice.domain.service
- ec.sri.einvoice.application.port.in
- ec.sri.einvoice.application.port.out
- ec.sri.einvoice.application.service
- ec.sri.einvoice.infrastructure.persistence
- ec.sri.einvoice.infrastructure.xml
- ec.sri.einvoice.infrastructure.sri
