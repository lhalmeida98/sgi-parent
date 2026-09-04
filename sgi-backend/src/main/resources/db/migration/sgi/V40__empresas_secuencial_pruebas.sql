alter table empresas
  add column if not exists secuencial_pruebas varchar(20);

update empresas
set secuencial_pruebas = secuencial
where secuencial_pruebas is null or trim(secuencial_pruebas) = '';

alter table empresas
  alter column secuencial_pruebas set not null;
