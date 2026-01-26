alter table if exists firmas_electronicas
  add column if not exists ruta_archivo varchar(500);

alter table if exists firmas_electronicas
  drop column if exists contenido;
