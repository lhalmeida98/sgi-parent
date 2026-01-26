create table acciones (
  id bigserial primary key,
  empresa_id bigint not null,
  codigo varchar(80) not null,
  descripcion varchar(255),
  activo boolean not null default true,
  creado_en timestamp not null default now(),
  actualizado_en timestamp,
  constraint acciones_empresa_fk foreign key (empresa_id) references empresas (id)
);

create unique index if not exists acciones_empresa_codigo_uq on acciones (empresa_id, codigo);
create index if not exists acciones_empresa_idx on acciones (empresa_id);
