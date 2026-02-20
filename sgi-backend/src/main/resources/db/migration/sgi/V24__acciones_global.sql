alter table if exists acciones
  drop constraint if exists acciones_empresa_fk;

drop index if exists acciones_empresa_codigo_uq;
drop index if exists acciones_empresa_idx;

alter table if exists acciones
  drop column if exists empresa_id;

alter table if exists acciones
  add column if not exists nombre varchar(120),
  add column if not exists url varchar(255),
  add column if not exists icono varchar(120),
  add column if not exists tipo varchar(80);

update acciones
  set nombre = codigo
  where nombre is null;

alter table if exists acciones
  alter column nombre set not null;

create unique index if not exists acciones_codigo_uq on acciones (codigo);
