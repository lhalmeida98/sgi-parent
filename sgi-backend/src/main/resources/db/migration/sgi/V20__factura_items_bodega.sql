alter table if exists factura_items
  add column if not exists bodega_id bigint;

with bodegas_default as (
  select empresa_id, min(id) as bodega_id
  from bodegas
  group by empresa_id
)
update factura_items fi
set bodega_id = bd.bodega_id
from facturas f
join bodegas_default bd on bd.empresa_id = f.empresa_id
where fi.factura_id = f.id
  and fi.bodega_id is null;

alter table if exists factura_items
  alter column bodega_id set not null;

alter table if exists factura_items
  add constraint factura_items_bodega_fk foreign key (bodega_id) references bodegas (id);

create index if not exists factura_items_bodega_idx
  on factura_items (bodega_id);
