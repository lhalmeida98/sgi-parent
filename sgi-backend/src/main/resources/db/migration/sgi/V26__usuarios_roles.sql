create table if not exists usuario_roles (
  id bigserial primary key,
  usuario_id bigint not null,
  rol_id bigint not null,
  constraint usuario_roles_usuario_fk foreign key (usuario_id) references usuarios (id),
  constraint usuario_roles_rol_fk foreign key (rol_id) references roles (id)
);

create unique index if not exists usuario_roles_uq on usuario_roles (usuario_id, rol_id);
create index if not exists usuario_roles_usuario_idx on usuario_roles (usuario_id);
create index if not exists usuario_roles_rol_idx on usuario_roles (rol_id);

insert into roles (nombre, descripcion, activo)
select distinct upper(u.rol), null, true
from usuarios u
where u.rol is not null
  and not exists (select 1 from roles r where r.nombre = upper(u.rol));

insert into usuario_roles (usuario_id, rol_id)
select u.id, r.id
from usuarios u
join roles r on r.nombre = upper(u.rol)
where u.rol is not null
on conflict do nothing;

alter table if exists usuarios
  drop column if exists rol;
