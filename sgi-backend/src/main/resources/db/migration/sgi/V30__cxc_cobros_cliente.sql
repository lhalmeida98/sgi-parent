create table documentos_cliente (
  id bigserial primary key,
  empresa_id bigint not null,
  cliente_id bigint not null,
  factura_id bigint,
  clave_acceso varchar(50),
  numero_factura varchar(50) not null,
  fecha_emision date not null,
  fecha_vencimiento date,
  total numeric(12, 2) not null,
  estado varchar(20) not null,
  creado_en timestamp not null default now(),
  actualizado_en timestamp,
  constraint documentos_cliente_empresa_fk foreign key (empresa_id) references empresas (id),
  constraint documentos_cliente_cliente_fk foreign key (cliente_id) references clientes (id),
  constraint documentos_cliente_factura_fk foreign key (factura_id) references facturas (id)
);

create unique index if not exists documentos_cliente_numero_uq
  on documentos_cliente (empresa_id, numero_factura);
create unique index if not exists documentos_cliente_factura_uq
  on documentos_cliente (factura_id);
create index if not exists documentos_cliente_empresa_idx on documentos_cliente (empresa_id);
create index if not exists documentos_cliente_cliente_idx on documentos_cliente (cliente_id);

create table cuentas_por_cobrar (
  id bigserial primary key,
  empresa_id bigint not null,
  cliente_id bigint not null,
  documento_cliente_id bigint not null,
  monto_original numeric(12, 2) not null,
  monto_cobrado numeric(12, 2) not null,
  saldo numeric(12, 2) not null,
  estado varchar(20) not null,
  fecha_vencimiento date,
  credito_dias integer,
  creado_en timestamp not null default now(),
  actualizado_en timestamp,
  constraint cuentas_por_cobrar_empresa_fk foreign key (empresa_id) references empresas (id),
  constraint cuentas_por_cobrar_cliente_fk foreign key (cliente_id) references clientes (id),
  constraint cuentas_por_cobrar_documento_fk foreign key (documento_cliente_id) references documentos_cliente (id)
);

create unique index if not exists cuentas_por_cobrar_documento_uq
  on cuentas_por_cobrar (documento_cliente_id);
create index if not exists cuentas_por_cobrar_empresa_idx on cuentas_por_cobrar (empresa_id);
create index if not exists cuentas_por_cobrar_cliente_idx on cuentas_por_cobrar (cliente_id);

create table cobros_cliente (
  id bigserial primary key,
  empresa_id bigint not null,
  cliente_id bigint not null,
  fecha date not null,
  forma_pago varchar(20) not null,
  referencia varchar(100),
  monto_total numeric(12, 2) not null,
  observacion varchar(255),
  creado_en timestamp not null default now(),
  constraint cobros_cliente_empresa_fk foreign key (empresa_id) references empresas (id),
  constraint cobros_cliente_cliente_fk foreign key (cliente_id) references clientes (id)
);

create index if not exists cobros_cliente_empresa_idx on cobros_cliente (empresa_id);
create index if not exists cobros_cliente_cliente_idx on cobros_cliente (cliente_id);

create table cobros_cliente_detalle (
  id bigserial primary key,
  cobro_cliente_id bigint not null,
  cuenta_por_cobrar_id bigint not null,
  monto_aplicado numeric(12, 2) not null,
  constraint cobros_cliente_detalle_cobro_fk
    foreign key (cobro_cliente_id) references cobros_cliente (id),
  constraint cobros_cliente_detalle_cxc_fk
    foreign key (cuenta_por_cobrar_id) references cuentas_por_cobrar (id)
);

create index if not exists cobros_cliente_detalle_cobro_idx
  on cobros_cliente_detalle (cobro_cliente_id);
create index if not exists cobros_cliente_detalle_cxc_idx
  on cobros_cliente_detalle (cuenta_por_cobrar_id);
