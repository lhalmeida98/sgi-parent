insert into acciones (nombre, codigo, descripcion, activo, creado_en)
select 'Dashboard', 'DASHBOARD', 'Acceso a metricas del dashboard', true, now()
where not exists (select 1 from acciones where codigo = 'DASHBOARD');
