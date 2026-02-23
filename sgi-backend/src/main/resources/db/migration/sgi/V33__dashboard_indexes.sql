-- Indexes to speed up dashboard aggregates
create index if not exists idx_facturas_empresa_fecha_estado
  on facturas (empresa_id, fecha_emision, estado);

create index if not exists idx_facturas_empresa_fecha_id
  on facturas (empresa_id, fecha_emision desc, id desc);

create index if not exists idx_factura_items_factura
  on factura_items (factura_id);

create index if not exists idx_factura_items_producto
  on factura_items (producto_id);

create index if not exists idx_cxc_empresa_fecha_estado
  on cuentas_por_cobrar (empresa_id, fecha_vencimiento, estado);

create index if not exists idx_cxp_empresa_fecha_estado
  on cuentas_por_pagar (empresa_id, fecha_vencimiento, estado);

create index if not exists idx_cobros_cliente_empresa_fecha
  on cobros_cliente (empresa_id, fecha);

create index if not exists idx_pagos_proveedor_empresa_fecha
  on pagos_proveedor (empresa_id, fecha_pago);
