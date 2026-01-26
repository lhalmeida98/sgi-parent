create table if not exists empresas (
  id uuid primary key,
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

create table if not exists firmas_electronicas (
  id uuid primary key,
  empresa_id uuid not null,
  nombre_archivo varchar(255) not null,
  tipo_contenido varchar(100),
  contenido bytea not null,
  clave varchar(255) not null,
  constraint firmas_electronicas_empresa_fk foreign key (empresa_id) references empresas (id)
);

create unique index if not exists firmas_electronicas_empresa_uq on firmas_electronicas (empresa_id);

alter table if exists facturas
  add column if not exists empresa_id uuid;

alter table if exists facturas
  add constraint facturas_empresa_fk foreign key (empresa_id) references empresas (id);
