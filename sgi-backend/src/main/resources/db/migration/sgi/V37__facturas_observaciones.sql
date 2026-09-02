alter table if exists facturas
  add column if not exists observaciones varchar(1000);
