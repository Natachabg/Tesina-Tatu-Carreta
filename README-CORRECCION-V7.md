# Tatú Carreta 4G — Corrección V7

Cambios de esta versión:
- Unificación de tablas duplicadas heredadas en SQLite. `identificaciones` y `plantel_permanente` quedan como tablas canónicas.
- Migración del registro histórico de `identificaciones_plantel` a `identificaciones` antes de eliminar la tabla heredada.
- Actas con responsable de entrega y receptor en la Reserva, ambos obligatorios al crear/modificar.
- Estado sanitario del ingreso con opciones controladas y sin valor precargado arbitrariamente.
- Peso vacío cuando no fue informado; no se muestra `0.0`.
- Fechas visibles en formato DD/MM/AAAA.
- Identificaciones visibles en Plantel y Movimientos.
- Detalle de movimientos simplificado para evitar repetir la información del acta.
- Capacidad de habitáculos validada también al registrar nacimientos.
- Plantel con botones organizados en una segunda fila para que se vean completos.
- Tabla dinámica: no muestra filas vacías innecesarias.
- Estado de conservación agregado al catálogo de especies.
- Dashboard sin accesos rápidos ni duplicación del logo/título; logo oficial integrado visualmente.
- Usuarios sigue siendo visible y administrable exclusivamente por Administrador.
- Se mantienen bajas lógicas y controles contra duplicados activos.

El PDF mensual se mantiene pendiente de ajustar al archivo de muestra específico de la Reserva si se incorpora una plantilla adicional.
