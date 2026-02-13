create table proveedores (
  id bigserial primary key,
  empresa_id bigint not null,
  tipo_identificacion varchar(20) not null,
  identificacion varchar(30) not null,
  razon_social varchar(255) not null,
  nombre_comercial varchar(255),
  email varchar(255),
  telefono varchar(50),
  direccion varchar(255),
  condiciones_pago varchar(20) not null,
  estado varchar(20) not null,
  creado_en timestamp not null default now(),
  actualizado_en timestamp,
  constraint proveedores_empresa_fk foreign key (empresa_id) references empresas (id)
);

create unique index if not exists proveedores_empresa_identificacion_uq
  on proveedores (empresa_id, identificacion);
create index if not exists proveedores_empresa_idx on proveedores (empresa_id);

create table documentos_proveedor (
  id bigserial primary key,
  empresa_id bigint not null,
  proveedor_id bigint not null,
  tipo_documento varchar(20) not null,
  numero_documento varchar(50) not null,
  numero_autorizacion varchar(50),
  fecha_emision date not null,
  fecha_vencimiento date,
  subtotal numeric(12, 2) not null,
  impuestos numeric(12, 2) not null,
  total numeric(12, 2) not null,
  moneda varchar(10) not null,
  estado varchar(20) not null,
  xml text,
  creado_en timestamp not null default now(),
  actualizado_en timestamp,
  constraint documentos_proveedor_empresa_fk foreign key (empresa_id) references empresas (id),
  constraint documentos_proveedor_proveedor_fk foreign key (proveedor_id) references proveedores (id)
);

create unique index if not exists documentos_proveedor_uq
  on documentos_proveedor (empresa_id, proveedor_id, numero_documento);
create index if not exists documentos_proveedor_empresa_idx on documentos_proveedor (empresa_id);
create index if not exists documentos_proveedor_proveedor_idx on documentos_proveedor (proveedor_id);

create table documentos_proveedor_items (
  id bigserial primary key,
  documento_proveedor_id bigint not null,
  producto_id bigint,
  codigo_principal varchar(50),
  descripcion varchar(255),
  cantidad numeric(12, 2) not null,
  costo_unitario numeric(12, 2) not null,
  subtotal numeric(12, 2) not null,
  constraint doc_proveedor_items_documento_fk
    foreign key (documento_proveedor_id) references documentos_proveedor (id)
);

create index if not exists doc_proveedor_items_documento_idx
  on documentos_proveedor_items (documento_proveedor_id);

create table cuentas_por_pagar (
  id bigserial primary key,
  empresa_id bigint not null,
  proveedor_id bigint not null,
  documento_proveedor_id bigint not null,
  monto_original numeric(12, 2) not null,
  monto_pagado numeric(12, 2) not null,
  saldo numeric(12, 2) not null,
  estado varchar(20) not null,
  fecha_vencimiento date,
  creado_en timestamp not null default now(),
  actualizado_en timestamp,
  constraint cuentas_por_pagar_empresa_fk foreign key (empresa_id) references empresas (id),
  constraint cuentas_por_pagar_proveedor_fk foreign key (proveedor_id) references proveedores (id),
  constraint cuentas_por_pagar_documento_fk foreign key (documento_proveedor_id) references documentos_proveedor (id)
);

create unique index if not exists cuentas_por_pagar_documento_uq
  on cuentas_por_pagar (documento_proveedor_id);
create index if not exists cuentas_por_pagar_empresa_idx on cuentas_por_pagar (empresa_id);
create index if not exists cuentas_por_pagar_proveedor_idx on cuentas_por_pagar (proveedor_id);

create table pagos_proveedor (
  id bigserial primary key,
  empresa_id bigint not null,
  proveedor_id bigint not null,
  fecha_pago date not null,
  forma_pago varchar(20) not null,
  referencia varchar(100),
  monto_total numeric(12, 2) not null,
  observacion varchar(255),
  creado_en timestamp not null default now(),
  constraint pagos_proveedor_empresa_fk foreign key (empresa_id) references empresas (id),
  constraint pagos_proveedor_proveedor_fk foreign key (proveedor_id) references proveedores (id)
);

create index if not exists pagos_proveedor_empresa_idx on pagos_proveedor (empresa_id);
create index if not exists pagos_proveedor_proveedor_idx on pagos_proveedor (proveedor_id);

create table pagos_proveedor_detalle (
  id bigserial primary key,
  pago_proveedor_id bigint not null,
  cuenta_por_pagar_id bigint not null,
  monto_aplicado numeric(12, 2) not null,
  constraint pagos_proveedor_detalle_pago_fk
    foreign key (pago_proveedor_id) references pagos_proveedor (id),
  constraint pagos_proveedor_detalle_cxp_fk
    foreign key (cuenta_por_pagar_id) references cuentas_por_pagar (id)
);

create index if not exists pagos_proveedor_detalle_pago_idx
  on pagos_proveedor_detalle (pago_proveedor_id);
create index if not exists pagos_proveedor_detalle_cxp_idx
  on pagos_proveedor_detalle (cuenta_por_pagar_id);
