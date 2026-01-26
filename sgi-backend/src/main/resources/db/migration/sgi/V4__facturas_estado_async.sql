alter table if exists facturas
  add column if not exists numero_autorizacion varchar(50);

alter table if exists facturas
  add column if not exists xml_autorizado text;

alter table if exists facturas
  add column if not exists intentos_consulta integer default 0;

alter table if exists facturas
  add column if not exists ultimo_intento_consulta timestamp;

do $$
begin
  if exists (
    select 1
    from information_schema.tables
    where table_schema = 'public'
      and table_name = 'facturas'
  ) then
    update facturas
    set intentos_consulta = 0
    where intentos_consulta is null;

    update facturas
    set estado = 'ENVIADA_SRI'
    where estado = 'ENVIADA_CORE';

    update facturas
    set estado = 'NO_AUTORIZADA'
    where estado = 'RECHAZADA';
  end if;
end $$;
