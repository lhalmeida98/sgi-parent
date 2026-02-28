update productos
set codigo_barras = null
where codigo_barras is not null
  and btrim(codigo_barras) = '';

drop index if exists productos_empresa_codigo_uq;
drop index if exists productos_empresa_codigo_barras_uq;
drop index if exists productos_codigo_barras_uq;

create index if not exists productos_empresa_codigo_idx
  on productos (empresa_id, codigo);

create index if not exists productos_empresa_codigo_barras_idx
  on productos (empresa_id, codigo_barras);
