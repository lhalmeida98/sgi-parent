alter table if exists clientes
  add column if not exists credito_dias integer;

alter table if exists empresas
  add column if not exists credito_dias_default integer;

update empresas
  set credito_dias_default = 30
  where credito_dias_default is null;

alter table if exists cuentas_por_cobrar
  add column if not exists credito_dias integer;
