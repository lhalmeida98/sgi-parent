alter table if exists empresas
  add column if not exists regimen_tributario varchar(40);

alter table if exists empresas
  add column if not exists contribuyente_especial boolean not null default false;

alter table if exists empresas
  add column if not exists numero_contribuyente_especial varchar(13);

alter table if exists empresas
  add column if not exists agente_retencion boolean not null default false;

update empresas
set regimen_tributario = case
  when regimen_rimpe is true then 'RIMPE_EMPRENDEDOR'
  else 'GENERAL'
end
where regimen_tributario is null;

alter table if exists empresas
  alter column regimen_tributario set not null;

alter table if exists facturas
  add column if not exists fecha_autorizacion timestamp;

alter table if exists facturas
  add column if not exists info_obligado_contabilidad boolean;

alter table if exists facturas
  add column if not exists info_regimen_tributario varchar(40);

alter table if exists facturas
  add column if not exists info_contribuyente_especial boolean;

alter table if exists facturas
  add column if not exists info_numero_contribuyente_especial varchar(13);

alter table if exists facturas
  add column if not exists info_agente_retencion boolean;

update facturas f
set
  info_obligado_contabilidad = coalesce(f.info_obligado_contabilidad, e.obligado_contabilidad, false),
  info_regimen_tributario = coalesce(
    f.info_regimen_tributario,
    e.regimen_tributario,
    case when e.regimen_rimpe is true then 'RIMPE_EMPRENDEDOR' else 'GENERAL' end
  ),
  info_contribuyente_especial = coalesce(f.info_contribuyente_especial, e.contribuyente_especial, false),
  info_numero_contribuyente_especial = coalesce(f.info_numero_contribuyente_especial, e.numero_contribuyente_especial),
  info_agente_retencion = coalesce(f.info_agente_retencion, e.agente_retencion, false)
from empresas e
where f.empresa_id = e.id
  and (
    f.info_obligado_contabilidad is null
    or f.info_regimen_tributario is null
    or f.info_contribuyente_especial is null
    or f.info_agente_retencion is null
  );

update facturas
set
  info_obligado_contabilidad = coalesce(info_obligado_contabilidad, false),
  info_regimen_tributario = coalesce(info_regimen_tributario, 'GENERAL'),
  info_contribuyente_especial = coalesce(info_contribuyente_especial, false),
  info_agente_retencion = coalesce(info_agente_retencion, false);
