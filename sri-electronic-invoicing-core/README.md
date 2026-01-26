# sri-electronic-invoicing-core

Core de comprobantes electronicos SRI (esquema offline) con Java 21, Spring Boot 3 y arquitectura limpia.

## Capas

- domain: entidades, value objects, reglas de negocio, eventos y errores
- application: casos de uso y puertos (interfaces) que orquestan el dominio
- infrastructure: adaptadores (JPA, XML, firma, SRI, scheduling, time)
- interfaces (opcional): puntos de entrada no REST si se requieren

## Notas

- No se exponen controladores REST.
- El dominio no depende de Spring ni de frameworks.
- La validacion de estructura XML usa XSD en `src/main/resources/sri/xsd`.
- Ver detalle en `docs/ARCHITECTURE.md`.
