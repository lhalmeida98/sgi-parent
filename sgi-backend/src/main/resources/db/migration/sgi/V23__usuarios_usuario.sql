alter table if exists usuarios
  add column if not exists usuario varchar(255);

update usuarios
  set usuario = email
  where usuario is null;

alter table if exists usuarios
  alter column usuario set not null;

create unique index if not exists usuarios_usuario_uq on usuarios (usuario);
