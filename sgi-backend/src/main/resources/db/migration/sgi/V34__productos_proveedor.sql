alter table if exists productos
  add column if not exists proveedor_id bigint;

alter table if exists productos
  drop constraint if exists productos_proveedor_fk;

alter table if exists productos
  add constraint productos_proveedor_fk foreign key (proveedor_id) references proveedores (id);

create index if not exists productos_proveedor_idx on productos (proveedor_id);
