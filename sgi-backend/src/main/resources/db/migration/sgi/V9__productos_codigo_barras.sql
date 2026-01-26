alter table if exists productos
  add column if not exists codigo_barras varchar(100);
