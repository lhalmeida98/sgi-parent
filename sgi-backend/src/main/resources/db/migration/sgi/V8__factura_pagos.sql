create table if not exists factura_pagos (
  id bigserial primary key,
  factura_id bigint not null,
  forma_pago varchar(50) not null,
  monto numeric(12, 2) not null,
  constraint factura_pagos_factura_fk foreign key (factura_id) references facturas (id)
);

create index if not exists factura_pagos_factura_idx on factura_pagos (factura_id);
