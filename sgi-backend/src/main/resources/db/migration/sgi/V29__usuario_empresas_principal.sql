alter table if exists usuario_empresas
  add column if not exists principal boolean not null default false;

update usuario_empresas
set principal = false;

update usuario_empresas ue
set principal = true
from usuarios u
where ue.usuario_id = u.id
  and ue.empresa_id = u.empresa_id;

with sin_principal as (
  select ue.usuario_id, min(ue.id) as id
  from usuario_empresas ue
  left join usuario_empresas ue2
    on ue2.usuario_id = ue.usuario_id and ue2.principal = true
  where ue2.id is null
  group by ue.usuario_id
)
update usuario_empresas ue
set principal = true
from sin_principal sp
where ue.id = sp.id;

create unique index if not exists usuario_empresas_principal_uq
  on usuario_empresas (usuario_id) where principal = true;
