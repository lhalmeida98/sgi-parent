alter table if exists documentos_proveedor_items
  add column if not exists bodega_id bigint;

alter table if exists documentos_proveedor_items
  add constraint doc_proveedor_items_bodega_fk foreign key (bodega_id) references bodegas (id);

create index if not exists doc_proveedor_items_bodega_idx
  on documentos_proveedor_items (bodega_id);
