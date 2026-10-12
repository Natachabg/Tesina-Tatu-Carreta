# Tatú Carreta · IAUX v5

Versión de interfaz y lógica reforzada sobre la versión 4G.

## Cambios principales
- Logo nuevo de la Reserva incorporado en login y menú lateral.
- Panel general rediseñado, sin accesos rápidos redundantes y con indicadores reales de la base.
- Plantel, animales, especies, actas, movimientos, habitáculos y usuarios con búsqueda, tabla compacta, doble click y registros inactivos.
- Usuarios visible únicamente para Administrador.
- Restauración desde Registros Inactivos.
- Borrado lógico mediante estado `Activo` / `Inactivo`.
- Validación de asociaciones antes de desactivar animales, especies, actas y habitáculos.
- Creación de especie desde Animal, Ingreso y Plantel sin abandonar el flujo.
- Identificaciones opcionales: Chip, Caravana, Anillo y Otro.
- Calendario para todas las fechas.
- Sexo en nacimientos y fallecimientos.
- Control de capacidad de habitáculos.
- Límites de longitud en todos los campos de texto y validación de campos obligatorios.
- Reporte mensual del Plantel Permanente en PDF, basado en la planilla de inventario suministrada.
- Migración automática de la base sin borrar registros.

## Acceso inicial
- Usuario: `admin`
- Contraseña: `admin`

La cuenta `admin` queda protegida y no puede enviarse a registros inactivos desde la interfaz.

## Ejecutar
Desde la carpeta que contiene `pom.xml`:

```powershell
mvn clean javafx:run
```
