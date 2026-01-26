create table if not exists categorias (
  id bigserial primary key,
  nombre varchar(120) not null,
  descripcion varchar(255)
);

create table if not exists impuestos (
  id bigserial primary key,
  codigo varchar(10) not null,
  codigo_porcentaje varchar(10) not null,
  tarifa numeric(6, 2) not null,
  descripcion varchar(255),
  activo boolean not null default true
);

alter table if exists productos
  add column if not exists categoria_id bigint,
  add column if not exists impuesto_id bigint;

alter table if exists productos
  drop column if exists impuesto_codigo,
  drop column if exists impuesto_codigo_porcentaje,
  drop column if exists impuesto_tarifa;

alter table if exists productos
  drop constraint if exists productos_categoria_fk;

alter table if exists productos
  drop constraint if exists productos_impuesto_fk;

alter table if exists productos
  add constraint productos_categoria_fk foreign key (categoria_id) references categorias (id);

alter table if exists productos
  add constraint productos_impuesto_fk foreign key (impuesto_id) references impuestos (id);
