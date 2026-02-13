alter table if exists inventarios
  add column if not exists bodega_id bigint;

insert into bodegas (empresa_id, nombre, descripcion, direccion, activa, creado_en)
select e.id, 'PRINCIPAL', 'Bodega creada automaticamente', null, true, now()
from empresas e
where not exists (select 1 from bodegas b where b.empresa_id = e.id);

with bodegas_default as (
  select empresa_id, min(id) as bodega_id
  from bodegas
  group by empresa_id
)
update inventarios i
set bodega_id = bd.bodega_id
from bodegas_default bd
where i.empresa_id = bd.empresa_id
  and i.bodega_id is null;

alter table if exists inventarios
  alter column bodega_id set not null;

alter table if exists inventarios
  add constraint inventarios_bodega_fk foreign key (bodega_id) references bodegas (id);

drop index if exists inventarios_producto_uq;

create unique index if not exists inventarios_empresa_bodega_producto_uq
  on inventarios (empresa_id, bodega_id, producto_id);

create index if not exists inventarios_bodega_idx
  on inventarios (bodega_id);
