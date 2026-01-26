create table roles (
  id bigserial primary key,
  empresa_id bigint not null,
  nombre varchar(80) not null,
  descripcion varchar(255),
  creado_en timestamp not null default now(),
  actualizado_en timestamp,
  constraint roles_empresa_fk foreign key (empresa_id) references empresas (id)
);

create unique index if not exists roles_empresa_nombre_uq on roles (empresa_id, nombre);

create table rol_permisos (
  id bigserial primary key,
  rol_id bigint not null,
  accion varchar(80) not null,
  constraint rol_permisos_rol_fk foreign key (rol_id) references roles (id)
);

create index if not exists rol_permisos_rol_idx on rol_permisos (rol_id);
create index if not exists rol_permisos_accion_idx on rol_permisos (accion);

create table bodegas (
  id bigserial primary key,
  empresa_id bigint not null,
  nombre varchar(120) not null,
  descripcion varchar(255),
  direccion varchar(255),
  activa boolean not null default true,
  creado_en timestamp not null default now(),
  actualizado_en timestamp,
  constraint bodegas_empresa_fk foreign key (empresa_id) references empresas (id)
);

create unique index if not exists bodegas_empresa_nombre_uq on bodegas (empresa_id, nombre);
create index if not exists bodegas_empresa_idx on bodegas (empresa_id);
