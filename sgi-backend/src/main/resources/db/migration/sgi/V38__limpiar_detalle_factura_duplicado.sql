create temporary table tmp_factura_item_duplicados on commit drop as
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
affected_facturas as (
  select f.id
  from facturas f
  join item_rows ir on ir.factura_id = f.id
  group by f.id, f.total_sin_impuestos
  having count(*) > count(*) filter (where ir.rn = 1)
     and coalesce(sum(ir.precio_total_sin_impuesto) filter (where ir.rn = 1), 0) = f.total_sin_impuestos
     and coalesce(sum(ir.precio_total_sin_impuesto), 0) <> f.total_sin_impuestos
)
select ir.id
from item_rows ir
join affected_facturas af on af.id = ir.factura_id
where ir.rn > 1;

delete from factura_impuestos
where item_id in (select id from tmp_factura_item_duplicados);

delete from factura_items
where id in (select id from tmp_factura_item_duplicados);

create temporary table tmp_factura_pago_duplicados on commit drop as
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
affected_facturas as (
  select f.id
  from facturas f
  join pago_rows pr on pr.factura_id = f.id
  group by f.id, f.importe_total
  having count(*) > count(*) filter (where pr.rn = 1)
     and coalesce(sum(pr.monto) filter (where pr.rn = 1), 0) = f.importe_total
     and coalesce(sum(pr.monto), 0) <> f.importe_total
)
select pr.id
from pago_rows pr
join affected_facturas af on af.id = pr.factura_id
where pr.rn > 1;

delete from factura_pagos
where id in (select id from tmp_factura_pago_duplicados);
