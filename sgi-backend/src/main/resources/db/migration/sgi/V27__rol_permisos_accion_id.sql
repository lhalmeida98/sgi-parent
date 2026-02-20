alter table if exists rol_permisos
  add column if not exists accion_id bigint;

update rol_permisos rp
set accion_id = a.id
from acciones a
where rp.accion = a.codigo;

delete from rol_permisos
where accion_id is null;

alter table if exists rol_permisos
  drop column if exists accion;

drop index if exists rol_permisos_accion_idx;

alter table if exists rol_permisos
  alter column accion_id set not null;

alter table if exists rol_permisos
  add constraint rol_permisos_accion_fk foreign key (accion_id) references acciones (id);

create index if not exists rol_permisos_accion_id_idx on rol_permisos (accion_id);
