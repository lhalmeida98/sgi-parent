alter table if exists facturas
  add column if not exists xml_firmado text;
