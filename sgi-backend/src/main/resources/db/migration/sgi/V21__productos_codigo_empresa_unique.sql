drop index if exists productos_codigo_barras_uq;

create unique index if not exists productos_empresa_codigo_uq
  on productos (empresa_id, codigo);

create unique index if not exists productos_empresa_codigo_barras_uq
  on productos (empresa_id, codigo_barras);
