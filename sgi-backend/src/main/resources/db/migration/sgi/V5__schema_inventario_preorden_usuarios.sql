drop table if exists factura_impuestos;
drop table if exists factura_items;
drop table if exists facturas;
drop table if exists firmas_electronicas;
drop table if exists inventarios;
drop table if exists preorden_items;
drop table if exists preordenes;
drop table if exists productos;
drop table if exists clientes;
drop table if exists usuarios;
drop table if exists empresas;

create table empresas (
  id bigserial primary key,
  ambiente varchar(20) not null,
  tipo_emision varchar(20) not null,
  razon_social varchar(255) not null,
  nombre_comercial varchar(255) not null,
  ruc varchar(20) not null,
  dir_matriz varchar(255) not null,
  estab varchar(10) not null,
  pto_emi varchar(10) not null,
  secuencial varchar(20) not null
);

create table clientes (
  id bigserial primary key,
  tipo_identificacion varchar(30) not null,
  identificacion varchar(30) not null,
  razon_social varchar(255) not null,
  email varchar(255) not null,
  direccion varchar(255) not null
);

create table productos (
  id bigserial primary key,
  codigo varchar(50) not null,
  descripcion varchar(255) not null,
  precio_unitario numeric(12, 2) not null,
  impuesto_codigo varchar(10) not null,
  impuesto_codigo_porcentaje varchar(10) not null,
  impuesto_tarifa numeric(6, 2) not null
);

create table inventarios (
  id bigserial primary key,
  producto_id bigint not null,
  stock_actual numeric(12, 2) not null default 0,
  stock_minimo numeric(12, 2) not null default 0,
  stock_maximo numeric(12, 2),
  ubicacion varchar(120),
  costo_promedio numeric(12, 2),
  actualizado_en timestamp,
  constraint inventarios_producto_fk foreign key (producto_id) references productos (id)
);

create table preordenes (
  id bigserial primary key,
  empresa_id bigint,
  cliente_id bigint not null,
  fecha_creacion timestamp not null,
  dir_establecimiento varchar(255) not null,
  moneda varchar(10) not null,
  total_sin_impuestos numeric(12, 2) not null,
  total_descuento numeric(12, 2) not null,
  total_impuestos numeric(12, 2) not null,
  importe_total numeric(12, 2) not null,
  estado varchar(30) not null,
  observaciones varchar(500),
  constraint preordenes_empresa_fk foreign key (empresa_id) references empresas (id),
  constraint preordenes_cliente_fk foreign key (cliente_id) references clientes (id)
);

create table preorden_items (
  id bigserial primary key,
  preorden_id bigint not null,
  producto_id bigint not null,
  codigo_principal varchar(50) not null,
  descripcion varchar(255) not null,
  cantidad numeric(12, 2) not null,
  precio_unitario numeric(12, 2) not null,
  descuento numeric(12, 2) not null,
  precio_total_sin_impuesto numeric(12, 2) not null,
  constraint preorden_items_preorden_fk foreign key (preorden_id) references preordenes (id),
  constraint preorden_items_producto_fk foreign key (producto_id) references productos (id)
);

create table facturas (
  id bigserial primary key,
  empresa_id bigint,
  cliente_id bigint not null,
  preorden_id bigint,
  fecha_emision date not null,
  dir_establecimiento varchar(255) not null,
  moneda varchar(10) not null,
  total_sin_impuestos numeric(12, 2) not null,
  total_descuento numeric(12, 2) not null,
  total_impuestos numeric(12, 2) not null,
  importe_total numeric(12, 2) not null,
  estado varchar(30),
  clave_acceso varchar(100),
  core_comprobante_id varchar(100),
  sri_estado_consulta varchar(100),
  sri_estado_autorizacion varchar(100),
  sri_mensaje varchar(500),
  numero_autorizacion varchar(50),
  xml_autorizado text,
  intentos_consulta integer default 0,
  ultimo_intento_consulta timestamp,
  info_ambiente varchar(20),
  info_tipo_emision varchar(20),
  info_razon_social varchar(255),
  info_nombre_comercial varchar(255),
  info_ruc varchar(20),
  info_dir_matriz varchar(255),
  info_estab varchar(10),
  info_pto_emi varchar(10),
  info_secuencial varchar(20),
  constraint facturas_empresa_fk foreign key (empresa_id) references empresas (id),
  constraint facturas_cliente_fk foreign key (cliente_id) references clientes (id),
  constraint facturas_preorden_fk foreign key (preorden_id) references preordenes (id)
);

create table factura_items (
  id bigserial primary key,
  factura_id bigint not null,
  producto_id bigint not null,
  codigo_principal varchar(50) not null,
  descripcion varchar(255) not null,
  cantidad numeric(12, 2) not null,
  precio_unitario numeric(12, 2) not null,
  descuento numeric(12, 2) not null,
  precio_total_sin_impuesto numeric(12, 2) not null,
  constraint factura_items_factura_fk foreign key (factura_id) references facturas (id),
  constraint factura_items_producto_fk foreign key (producto_id) references productos (id)
);

create table factura_impuestos (
  id bigserial primary key,
  item_id bigint not null,
  codigo varchar(10) not null,
  codigo_porcentaje varchar(10) not null,
  tarifa numeric(6, 2) not null,
  base_imponible numeric(12, 2) not null,
  valor numeric(12, 2) not null,
  constraint factura_impuestos_item_fk foreign key (item_id) references factura_items (id)
);

create table firmas_electronicas (
  id bigserial primary key,
  empresa_id bigint not null,
  nombre_archivo varchar(255) not null,
  tipo_contenido varchar(100),
  ruta_archivo varchar(500),
  clave varchar(255) not null,
  constraint firmas_electronicas_empresa_fk foreign key (empresa_id) references empresas (id)
);

create unique index if not exists firmas_electronicas_empresa_uq on firmas_electronicas (empresa_id);

create table usuarios (
  id bigserial primary key,
  nombre varchar(120) not null,
  email varchar(255) not null,
  password_hash varchar(255) not null,
  rol varchar(50) not null,
  activo boolean not null default true,
  creado_en timestamp not null default now(),
  actualizado_en timestamp
);

create unique index if not exists usuarios_email_uq on usuarios (email);
