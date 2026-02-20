alter table if exists roles
  drop constraint if exists roles_empresa_fk;

drop index if exists roles_empresa_nombre_uq;
drop index if exists roles_empresa_idx;

alter table if exists roles
  drop column if exists empresa_id;

alter table if exists roles
  add column if not exists activo boolean not null default true;

update roles
  set activo = true
  where activo is null;

create unique index if not exists roles_nombre_uq on roles (nombre);
