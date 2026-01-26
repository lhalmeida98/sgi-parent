CREATE TABLE comprobante (
  id UUID PRIMARY KEY,
  tipo VARCHAR(20) NOT NULL,
  estado VARCHAR(20) NOT NULL,
  clave_acceso VARCHAR(49) NOT NULL,
  xml TEXT,
  xml_firmado TEXT,
  numero_autorizacion VARCHAR(64),
  emisor_ruc VARCHAR(13) NOT NULL,
  receptor_identificacion VARCHAR(20) NOT NULL,
  total DECIMAL(18,2) NOT NULL,
  intentos_envio INT NOT NULL,
  siguiente_reintento TIMESTAMP,
  creado_en TIMESTAMP NOT NULL,
  actualizado_en TIMESTAMP NOT NULL,
  payload_json TEXT NOT NULL
);

CREATE TABLE comprobante_evento (
  id UUID PRIMARY KEY,
  comprobante_id UUID NOT NULL,
  tipo VARCHAR(30) NOT NULL,
  detalle TEXT,
  ocurrido_en TIMESTAMP NOT NULL
);

CREATE TABLE bitacora (
  id UUID PRIMARY KEY,
  comprobante_id UUID NOT NULL,
  accion VARCHAR(50) NOT NULL,
  detalle TEXT,
  ocurrido_en TIMESTAMP NOT NULL
);
