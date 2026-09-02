with item_rows as (
  select
    fi.id,
    fi.factura_id,
    fi.precio_total_sin_impuesto,
    row_number() over (
      partition by
        fi.factura_id,
        fi.bodega_id,
        fi.producto_id,
        fi.codigo_principal,
        fi.descripcion,
        fi.cantidad,
        fi.precio_unitario,
        fi.descuento,
        fi.precio_total_sin_impuesto
      order by fi.id
    ) as rn
  from factura_items fi
),
facturas_con_detalle_duplicado as (
  select f.id
  from facturas f
  join item_rows ir on ir.factura_id = f.id
  group by f.id, f.total_sin_impuestos
  having count(*) > count(*) filter (where ir.rn = 1)
     and coalesce(sum(ir.precio_total_sin_impuesto) filter (where ir.rn = 1), 0) = f.total_sin_impuestos
     and coalesce(sum(ir.precio_total_sin_impuesto), 0) <> f.total_sin_impuestos
),
items_duplicados as (
  select ir.id
  from item_rows ir
  join facturas_con_detalle_duplicado fd on fd.id = ir.factura_id
  where ir.rn > 1
)
delete from factura_impuestos fi
using items_duplicados d
where fi.item_id = d.id;

with item_rows as (
  select
    fi.id,
    fi.factura_id,
    fi.precio_total_sin_impuesto,
    row_number() over (
      partition by
        fi.factura_id,
        fi.bodega_id,
        fi.producto_id,
        fi.codigo_principal,
        fi.descripcion,
        fi.cantidad,
        fi.precio_unitario,
        fi.descuento,
        fi.precio_total_sin_impuesto
      order by fi.id
    ) as rn
  from factura_items fi
),
facturas_con_detalle_duplicado as (
  select f.id
  from facturas f
  join item_rows ir on ir.factura_id = f.id
  group by f.id, f.total_sin_impuestos
  having count(*) > count(*) filter (where ir.rn = 1)
     and coalesce(sum(ir.precio_total_sin_impuesto) filter (where ir.rn = 1), 0) = f.total_sin_impuestos
     and coalesce(sum(ir.precio_total_sin_impuesto), 0) <> f.total_sin_impuestos
),
items_duplicados as (
  select ir.id
  from item_rows ir
  join facturas_con_detalle_duplicado fd on fd.id = ir.factura_id
  where ir.rn > 1
)
delete from factura_items fi
using items_duplicados d
where fi.id = d.id;

with pago_rows as (
  select
    fp.id,
    fp.factura_id,
    fp.monto,
    row_number() over (
      partition by
        fp.factura_id,
        fp.forma_pago,
        fp.monto
      order by fp.id
    ) as rn
  from factura_pagos fp
),
facturas_con_pagos_duplicados as (
  select f.id
  from facturas f
  join pago_rows pr on pr.factura_id = f.id
  group by f.id, f.importe_total
  having count(*) > count(*) filter (where pr.rn = 1)
     and coalesce(sum(pr.monto) filter (where pr.rn = 1), 0) = f.importe_total
     and coalesce(sum(pr.monto), 0) <> f.importe_total
),
pagos_duplicados as (
  select pr.id
  from pago_rows pr
  join facturas_con_pagos_duplicados fp on fp.id = pr.factura_id
  where pr.rn > 1
)
delete from factura_pagos fp
using pagos_duplicados d
where fp.id = d.id;
