# Tatú Carreta · IAUX Profesional

Versión de interfaz y flujo funcional para la Reserva Natural Tatú Carreta.

## Acceso inicial
Usuario: `admin`
Contraseña: `admin`

## Flujo general
- Las vistas usan la misma identidad visual verde.
- No existe el botón Actualizar: las búsquedas se filtran automáticamente.
- Doble click sobre una fila abre el detalle/modificación dentro de un diálogo modal.
- Los registros se dan de baja lógica y pueden recuperarse desde `REGISTROS INACTIVOS`.
- Los campos obligatorios comienzan con borde rojo y pasan a verde/neutro al completarse.
- Las fechas usan calendario.
- Especies y animales pueden crearse desde los módulos donde se necesitan, sin volver atrás.
- Las identificaciones son opcionales: Chip, Caravana, Anillo u Otro.
- Animales no permite cargar manualmente una cantidad en el ABM maestro; la cantidad se actualiza con ingresos, nacimientos y fallecimientos.
- No se permite quitar un animal asociado a un ingreso o al Plantel Permanente.
- No se permite quitar un acta que tenga animales asociados.
- No se permite quitar un habitáculo que tenga animales asignados al plantel.
- El reporte PDF mensual contiene exclusivamente el Plantel Permanente activo.
