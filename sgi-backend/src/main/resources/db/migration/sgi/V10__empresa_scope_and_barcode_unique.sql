alter table if exists categorias
  add column if not exists empresa_id bigint;

alter table if exists impuestos
  add column if not exists empresa_id bigint;

alter table if exists productos
  add column if not exists empresa_id bigint;

alter table if exists clientes
  add column if not exists empresa_id bigint;

alter table if exists inventarios
  add column if not exists empresa_id bigint;

alter table if exists usuarios
  add column if not exists empresa_id bigint;

alter table if exists categorias
  add constraint categorias_empresa_fk foreign key (empresa_id) references empresas (id);

alter table if exists impuestos
  add constraint impuestos_empresa_fk foreign key (empresa_id) references empresas (id);

alter table if exists productos
  add constraint productos_empresa_fk foreign key (empresa_id) references empresas (id);

alter table if exists clientes
  add constraint clientes_empresa_fk foreign key (empresa_id) references empresas (id);

alter table if exists inventarios
  add constraint inventarios_empresa_fk foreign key (empresa_id) references empresas (id);

alter table if exists usuarios
  add constraint usuarios_empresa_fk foreign key (empresa_id) references empresas (id);

create index if not exists categorias_empresa_idx on categorias (empresa_id);
create index if not exists impuestos_empresa_idx on impuestos (empresa_id);
create index if not exists productos_empresa_idx on productos (empresa_id);
create index if not exists clientes_empresa_idx on clientes (empresa_id);
create index if not exists inventarios_empresa_idx on inventarios (empresa_id);
create index if not exists usuarios_empresa_idx on usuarios (empresa_id);

create unique index if not exists productos_codigo_barras_uq on productos (codigo_barras);
