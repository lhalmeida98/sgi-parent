alter table if exists inventarios
  add column if not exists stock_reservado numeric(12, 2) not null default 0;

alter table if exists preordenes
  add column if not exists reserva_inventario boolean not null default false;

create unique index if not exists inventarios_producto_uq on inventarios (producto_id);
