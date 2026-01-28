alter table if exists empresas
  add column if not exists obligado_contabilidad boolean not null default false;

alter table if exists empresas
  add column if not exists regimen_rimpe boolean not null default false;
