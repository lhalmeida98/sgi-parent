alter table if exists preorden_items
  add column if not exists bodega_id bigint;

with bodegas_default as (
  select empresa_id, min(id) as bodega_id
  from bodegas
  group by empresa_id
)
update preorden_items pi
set bodega_id = bd.bodega_id
from preordenes p
join bodegas_default bd on bd.empresa_id = p.empresa_id
where pi.preorden_id = p.id
  and pi.bodega_id is null;

alter table if exists preorden_items
  alter column bodega_id set not null;

alter table if exists preorden_items
  add constraint preorden_items_bodega_fk foreign key (bodega_id) references bodegas (id);

create index if not exists preorden_items_bodega_idx
  on preorden_items (bodega_id);
