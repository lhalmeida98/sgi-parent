alter table if exists productos
  add column if not exists vendible boolean not null default true;
