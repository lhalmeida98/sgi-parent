create table if not exists usuario_empresas (
  id bigserial primary key,
  usuario_id bigint not null,
  empresa_id bigint not null,
  constraint usuario_empresas_usuario_fk foreign key (usuario_id) references usuarios (id),
  constraint usuario_empresas_empresa_fk foreign key (empresa_id) references empresas (id)
);

create unique index if not exists usuario_empresas_uq on usuario_empresas (usuario_id, empresa_id);
create index if not exists usuario_empresas_usuario_idx on usuario_empresas (usuario_id);
create index if not exists usuario_empresas_empresa_idx on usuario_empresas (empresa_id);

insert into usuario_empresas (usuario_id, empresa_id)
select u.id, u.empresa_id
from usuarios u
where u.empresa_id is not null
on conflict do nothing;
